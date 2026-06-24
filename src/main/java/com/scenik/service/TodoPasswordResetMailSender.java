package com.scenik.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TodoPasswordResetMailSender implements PasswordResetMailSender {
    private static final Logger log = LoggerFactory.getLogger(TodoPasswordResetMailSender.class);

    @Override
    public void sendResetLink(String email, String resetUrl) {
        // TODO: Replace with a configured SMTP, SES, or transactional-email adapter.
        // Do not log the reset URL or token before a real delivery provider is configured.
        log.warn("Password reset requested for a configured email account, but email delivery is not configured.");
    }
}
