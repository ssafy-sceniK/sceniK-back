package com.scenik.service;

public interface PasswordResetMailSender {
    void sendResetLink(String email, String resetUrl);
}
