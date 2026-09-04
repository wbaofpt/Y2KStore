package com.y2kstore.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 15)
    private String phone;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean status = true;

    @Column(name = "avatar_url", columnDefinition = "MEDIUMTEXT")
    private String avatarUrl;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "auth_provider", nullable = false, length = 30)
    private String authProvider = "LOCAL";

    @Column(name = "reset_token", length = 120)
    private String resetToken;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;


    // Constructors
    public User() {}

    public User(String password, String email, String fullName, String phone, Role role) {
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }
}
