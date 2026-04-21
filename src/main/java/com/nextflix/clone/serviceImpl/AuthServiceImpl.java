package com.nextflix.clone.serviceImpl;


import com.nextflix.clone.dao.UserRepository;
import com.nextflix.clone.dto.request.UserRequest;
import com.nextflix.clone.dto.response.LoginResponse;
import com.nextflix.clone.dto.response.MessageResponse;
import com.nextflix.clone.entity.User;
import com.nextflix.clone.enums.Role;
import com.nextflix.clone.exception.AccountDeactivatedException;
import com.nextflix.clone.exception.EmailAlreadyExistsException;
import com.nextflix.clone.exception.EmailNotVerifiedException;
import com.nextflix.clone.exception.InvalidTokenException;
import com.nextflix.clone.security.JwtUtil;
import com.nextflix.clone.service.AuthService;
import com.nextflix.clone.service.EmailService;
import com.nextflix.clone.util.ServiceUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    public MessageResponse signup(UserRequest userRequest) {
        // B1: Check if email already exists
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw  new EmailAlreadyExistsException("Email " + userRequest.getEmail() + " is already registered");
        }

        // B2: Create new user and save to database
        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setFullName(userRequest.getFullName());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);
        user.setEmailVerified(false);

        // B3: Generate email verification token and send verification email
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400)); // 24 hours expiry

        // B4: Save user and send verification email
        userRepository.save(user);
        emailService.sendVerificationEmail(userRequest.getEmail(), verificationToken);
        return new MessageResponse("Registration successful for " + userRequest.getEmail());
    }

    @Override
    public LoginResponse login(String email, String password) {
        // B1
        User user = userRepository
                .findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if(!user.isActive()) {
            throw new AccountDeactivatedException("Your account has been deactivated. Please contact support.");
        }

        if(!user.isEmailVerified()) {
            System.out.println("Email not verified for user: " + email);
            System.out.println("Verification token: " + user.isEmailVerified());
            throw new EmailNotVerifiedException("Your email has not been verified. Please check your inbox for the verification email.");
        }

        final String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    @Override
    public MessageResponse verifyEmail(String token) {
        User user = userRepository
                .findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));
        if(user.getVerificationTokenExpiry() == null || user.getVerificationTokenExpiry().isBefore(Instant.now())) {
            throw new InvalidTokenException("Verification token has expired");
        }
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        return new MessageResponse("Email verified successfully for " + user.getEmail());
    }
}
