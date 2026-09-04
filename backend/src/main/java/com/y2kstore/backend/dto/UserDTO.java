package com.y2kstore.backend.dto;

import com.y2kstore.backend.entity.User;

import java.time.LocalDateTime;

public class UserDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private Boolean emailVerified;
    private String authProvider;
    private String role;
    private Boolean status;
    private LocalDateTime createdAt;

    public UserDTO() {}

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.avatarUrl = user.getAvatarUrl();
        this.emailVerified = user.getEmailVerified();
        this.authProvider = user.getAuthProvider();
        this.role = user.getRole() != null ? user.getRole().getRoleName() : null;
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
