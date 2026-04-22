package com.nextflix.clone.service;

public interface EmailService {

    void sendVerificationEmail(String to, String token);

    void sendPasswordResetEmail(String to, String token);
}
