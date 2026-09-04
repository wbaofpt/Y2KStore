package com.y2kstore.backend.dto;

public class AuthResponse {
    private String token;
    private Integer id;
    private String email;
    private String fullName;
    private String role;
    private Boolean status;
    private String avatarUrl;
    private Boolean emailVerified;
    private String authProvider;

    public AuthResponse() {}

    public AuthResponse(String token, Integer id, String email, String fullName, String role, Boolean status, String avatarUrl, Boolean emailVerified, String authProvider) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.avatarUrl = avatarUrl;
        this.emailVerified = emailVerified;
        this.authProvider = authProvider;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
}
