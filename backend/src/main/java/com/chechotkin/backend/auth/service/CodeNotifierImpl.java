package com.chechotkin.backend.auth.service;

import com.chechotkin.backend.auth.usecase.CodeNotifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


public class CodeNotifierImpl implements CodeNotifier {

    private final JavaMailSender mailSender;
    private final String from;
    private final long expiresInMinutes;

    public CodeNotifierImpl(JavaMailSender mailSender, String from, long expiresInMinutes) {
        this.mailSender = mailSender;
        this.from = from;
        this.expiresInMinutes = expiresInMinutes;
    }

    @Override
    public void send(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Your PlanTogether sign-in code");
        message.setText("""
                Your sign-in code is %s

                It expires in %d minutes and can only be used once.
                If you did not ask to sign in, you can ignore this email.
                """.formatted(code, expiresInMinutes));

        mailSender.send(message);
    }
}
