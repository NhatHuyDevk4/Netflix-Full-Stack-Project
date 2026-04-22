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

    @Override
    public MessageResponse resendVerificationEmail(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400)); // 24 hours expiry
        userRepository.save(user);
        emailService.sendVerificationEmail(email, verificationToken);
        return new MessageResponse("Verification email resent to " + email);
    }

    @Override
    public MessageResponse forgotPassword(String email) {
        // B1: Check if user exists
        User user = serviceUtils.getUserByEmailOrThrow(email);

        // B2: Tạo token reset mật khẩu và gửi email
        String resetToken = UUID.randomUUID().toString();

        // B3: Lưu token vào database và gửi email
        user.setPasswordResetToken(resetToken);
        user.setVerificationTokenExpiry(Instant.now().plusSeconds(3600)); // 1 hour expiry
        userRepository.save(user);
        emailService.sendPasswordResetEmail(email, resetToken);
        return new MessageResponse("Password reset email sent to " + email);
    }

    @Override
    public MessageResponse resetPassword(String token, String newPassword) {
        // B1: Validate token and reset password
        User user = userRepository
                .findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        // B2: Check if token has expired
        if(user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new InvalidTokenException("Password reset token has expired");
        }

        // B3: Update password and clear reset token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        return new MessageResponse("Password reset successfully. You can now log in with your new password.");
    }

    @Override
    public MessageResponse changePassword(String email, String currentPassword, String newPassword) {

        User user = serviceUtils.getUserByEmailOrThrow(email);

        if(!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new MessageResponse("Password changed successfully");
    }

    @Override
    public LoginResponse getCurrentUser(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        return new LoginResponse(null, user.getEmail(), user.getFullName(), user.getRole().name());
    }
}

// plusSeconds là một phương thức của lớp Instant trong Java, được sử dụng để thêm một khoảng thời gian (tính bằng giây) vào một đối tượng Instant hiện tại. Khi bạn gọi user.setVerificationTokenExpiry(Instant.now().plusSeconds(86400)), nó sẽ thiết lập thời gian hết hạn của token là 24 giờ kể từ thời điểm hiện tại.