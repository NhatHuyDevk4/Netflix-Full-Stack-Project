package com.nextflix.clone.service;

import com.nextflix.clone.dto.request.LoginRequest;
import com.nextflix.clone.dto.request.UserRequest;
import com.nextflix.clone.dto.response.LoginResponse;
import com.nextflix.clone.dto.response.MessageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {
    MessageResponse signup(@Valid UserRequest userRequest);


   LoginResponse login(String email, String password);

    MessageResponse verifyEmail(String token);
}
