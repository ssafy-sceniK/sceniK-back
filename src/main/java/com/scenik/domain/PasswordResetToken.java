package com.scenik.domain;

import java.time.LocalDateTime;

public class PasswordResetToken {
    private Long passwordResetTokenId;
    private Long userId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    public Long getPasswordResetTokenId() { return passwordResetTokenId; }
    public void setPasswordResetTokenId(Long passwordResetTokenId) { this.passwordResetTokenId = passwordResetTokenId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
