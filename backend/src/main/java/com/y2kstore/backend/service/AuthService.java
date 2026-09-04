package com.y2kstore.backend.service;

import com.y2kstore.backend.dto.*;
import com.y2kstore.backend.entity.Address;
import com.y2kstore.backend.entity.EmailVerification;
import com.y2kstore.backend.entity.Role;
import com.y2kstore.backend.entity.User;
import com.y2kstore.backend.repository.AddressRepository;
import com.y2kstore.backend.repository.EmailVerificationRepository;
import com.y2kstore.backend.repository.RoleRepository;
import com.y2kstore.backend.repository.UserRepository;
import com.y2kstore.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private static final int MAX_ADDRESSES_PER_USER = 4;
    private static final int RESET_CODE_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AddressRepository addressRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;

    @Value("${y2kstore.password-reset.code-expiration-minutes:15}")
    private long codeExpirationMinutes;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider, AddressRepository addressRepository,
                       EmailVerificationRepository emailVerificationRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.addressRepository = addressRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailService = emailService;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String role = user.getRole() == null ? "ROLE_USER" : user.getRole().getRoleName();
        return toAuthResponse(tokenProvider.generateToken(user.getEmail(), role), user, role);
    }

    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) throw new RuntimeException("Email already exists.");
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndPurpose(email, "REGISTRATION").orElse(null);
        if (!isValidVerification(verification, request.getVerificationCode())) {
            throw new RuntimeException("Ma xac nhan email khong dung hoac da het han.");
        }
        User user = new User(passwordEncoder.encode(request.getPassword()), email,
                request.getFullName(), request.getPhone(), getOrCreateRole("ROLE_USER"));
        user.setAuthProvider("LOCAL");
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationRepository.delete(verification);
    }

    public void requestRegistrationCode(String emailInput) {
        String email = normalizeEmail(emailInput);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("Vui long nhap email hop le.");
        }
        if (userRepository.existsByEmail(email)) throw new RuntimeException("Email nay da duoc su dung.");
        sendVerification(email, "REGISTRATION");
    }

    public void forgotPassword(String emailInput) {
        User user = userRepository.findByEmail(normalizeEmail(emailInput)).orElse(null);
        if (user == null) return;
        String code = generateCode();
        user.setResetToken(code);
        user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(codeExpirationMinutes));
        userRepository.save(user);
        try {
            emailService.sendPasswordResetCode(user.getEmail(), code, codeExpirationMinutes);
        } catch (RuntimeException ex) {
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            userRepository.save(user);
            throw new RuntimeException("Khong the gui email luc nay.");
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (request.getToken() == null || !request.getToken().matches("\\d{6}")
                || request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Token hoac mat khau khong hop le.");
        }
        User user = userRepository.findByResetToken(request.getToken()).orElse(null);
        if (user == null || user.getResetTokenExpiresAt() == null
                || user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Ma xac nhan khong dung hoac da het han.");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
    }

    public void requestEmailVerification() {
        User user = currentUser();
        if (Boolean.TRUE.equals(user.getEmailVerified())) return;
        sendVerification(user.getEmail(), "PROFILE");
    }

    public Map<String, Object> verifyEmail(String code) {
        User user = currentUser();
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndPurpose(user.getEmail(), "PROFILE").orElse(null);
        if (!isValidVerification(verification, code)) {
            throw new RuntimeException("Ma xac nhan email khong dung hoac da het han.");
        }
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationRepository.delete(verification);
        return toProfilePayload(user);
    }

    public Map<String, Object> getProfile() {
        return toProfilePayload(currentUser());
    }

    public Map<String, Object> updateProfile(UserDTO dto) {
        User user = currentUser();
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) user.setFullName(dto.getFullName().trim());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone().trim());
        if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
        userRepository.save(user);
        return toProfilePayload(user);
    }

    public List<AddressDTO> getMyAddresses() {
        return addressRepository.findByUserId(currentUser().getId()).stream()
                .map(AddressDTO::new).collect(Collectors.toList());
    }

    public AddressDTO createMyAddress(AddressDTO dto) {
        User user = currentUser();
        if (addressRepository.countByUserId(user.getId()) >= MAX_ADDRESSES_PER_USER) {
            throw new RuntimeException("Moi tai khoan chi duoc toi da 4 dia chi.");
        }
        Address address = new Address();
        copyAddress(dto, address);
        address.setUser(user);
        if (Boolean.TRUE.equals(dto.getIsDefault())
                || addressRepository.findByUserIdAndIsDefaultTrue(user.getId()).isEmpty()) {
            clearDefault(user.getId());
            address.setIsDefault(true);
        }
        return new AddressDTO(addressRepository.save(address));
    }

    public AddressDTO updateMyAddress(Integer id, AddressDTO dto) {
        Address address = ownedAddress(id);
        copyAddress(dto, address);
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefault(address.getUser().getId());
            address.setIsDefault(true);
        }
        return new AddressDTO(addressRepository.save(address));
    }

    public void deleteMyAddress(Integer id) {
        Address address = ownedAddress(id);
        addressRepository.delete(address);
    }

    private void sendVerification(String email, String purpose) {
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndPurpose(email, purpose).orElseGet(EmailVerification::new);
        verification.setEmail(email);
        verification.setPurpose(purpose);
        verification.setCode(generateCode());
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(codeExpirationMinutes));
        emailVerificationRepository.save(verification);
        try {
            emailService.sendEmailVerificationCode(email, verification.getCode(), codeExpirationMinutes);
        } catch (RuntimeException ex) {
            emailVerificationRepository.delete(verification);
            throw new RuntimeException("Khong the gui email luc nay.");
        }
    }

    private String generateCode() {
        return String.format("%0" + RESET_CODE_LENGTH + "d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private boolean isValidVerification(EmailVerification verification, String code) {
        return verification != null && code != null
                && code.trim().equals(verification.getCode())
                && verification.getExpiresAt() != null
                && verification.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Address ownedAddress(Integer id) {
        User user = currentUser();
        return addressRepository.findById(id)
                .filter(address -> address.getUser() != null && address.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Forbidden"));
    }

    private void clearDefault(Integer userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .forEach(address -> {
                    address.setIsDefault(false);
                    addressRepository.save(address);
                });
    }

    private void copyAddress(AddressDTO dto, Address address) {
        address.setProvince(dto.getProvince());
        address.setDistrict(dto.getDistrict());
        address.setWard(dto.getWard());
        address.setDetail(dto.getDetail());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private Map<String, Object> toProfilePayload(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", user.getId());
        payload.put("email", user.getEmail());
        payload.put("fullName", user.getFullName());
        payload.put("phone", user.getPhone());
        payload.put("role", user.getRole() == null ? null : user.getRole().getRoleName());
        payload.put("status", user.getStatus());
        payload.put("avatarUrl", user.getAvatarUrl());
        payload.put("emailVerified", user.getEmailVerified());
        payload.put("authProvider", user.getAuthProvider());
        return payload;
    }

    private AuthResponse toAuthResponse(String token, User user, String role) {
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), role,
                user.getStatus(), user.getAvatarUrl(), user.getEmailVerified(), user.getAuthProvider());
    }
}

