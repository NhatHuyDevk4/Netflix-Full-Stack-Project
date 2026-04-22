package com.nextflix.clone.service;

import com.nextflix.clone.dto.request.LoginRequest;
import com.nextflix.clone.dto.request.UserRequest;
import com.nextflix.clone.dto.response.LoginResponse;
import com.nextflix.clone.dto.response.MessageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface AuthService {
    MessageResponse signup(@Valid UserRequest userRequest);

   LoginResponse login(String email, String password);

    MessageResponse verifyEmail(String token);

    MessageResponse resendVerificationEmail(String email);

    MessageResponse forgotPassword(String email);

    MessageResponse resetPassword(String token, String newPassword);

    MessageResponse changePassword(String email,  String currentPassword,  String newPassword);

    LoginResponse getCurrentUser(String email);
}
