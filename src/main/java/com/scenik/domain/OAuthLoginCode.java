package com.scenik.domain;

import java.time.LocalDateTime;

public class OAuthLoginCode {
    private Long oauthLoginCodeId;
    private Long userId;
    private String codeHash;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime consumedAt;

    public Long getOauthLoginCodeId() { return oauthLoginCodeId; }
    public void setOauthLoginCodeId(Long oauthLoginCodeId) { this.oauthLoginCodeId = oauthLoginCodeId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getConsumedAt() { return consumedAt; }
    public void setConsumedAt(LocalDateTime consumedAt) { this.consumedAt = consumedAt; }
}
