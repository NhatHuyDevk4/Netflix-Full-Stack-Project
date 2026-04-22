package com.nextflix.clone.controller;

import com.nextflix.clone.dto.request.*;
import com.nextflix.clone.dto.response.LoginResponse;
import com.nextflix.clone.dto.response.MessageResponse;
import com.nextflix.clone.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> register(
            @Valid
            @RequestBody UserRequest userRequest
            ) {
        return ResponseEntity.ok(authService.signup(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest)
    {
        return ResponseEntity.ok(authService.login(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerificationEmail(
            @Valid
            @RequestBody
            EmailRequest emailRequest
    ) {
        return ResponseEntity.ok(authService.resendVerificationEmail(emailRequest.getEmail()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid
            @RequestBody EmailRequest emailRequest
    ) {
        return ResponseEntity.ok(authService.forgotPassword(emailRequest.getEmail()));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
            ) {
        return ResponseEntity.ok(
                authService.resetPassword(
                        resetPasswordRequest.getToken(),
                        resetPasswordRequest.getNewPassword()
                )
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid
            @RequestBody ChangePasswordRequest changePasswordRequest
            ) {
        // Implement change password logic here, e.g., call authService.changePassword(currentPassword, newPassword)
        String email = authentication.getName(); // Get the email of the authenticated user
        System.out.println("Authenticated user email: " + email);
        System.out.println(changePasswordRequest);
        return ResponseEntity.ok(
                authService.changePassword(
                        email,
                        changePasswordRequest.getCurrentPassword(),
                        changePasswordRequest.getNewPassword()
                )
        );
    }

    @GetMapping("current-user")
    public ResponseEntity<LoginResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                authService.getCurrentUser(email)
        );
    }
}
