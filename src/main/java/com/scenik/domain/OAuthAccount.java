package com.scenik.domain;

import java.time.LocalDateTime;

public class OAuthAccount {
    private Long oauthAccountId;
    private Long userId;
    private String provider;
    private String providerUserId;
    private String providerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getOauthAccountId() { return oauthAccountId; }
    public void setOauthAccountId(Long oauthAccountId) { this.oauthAccountId = oauthAccountId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
