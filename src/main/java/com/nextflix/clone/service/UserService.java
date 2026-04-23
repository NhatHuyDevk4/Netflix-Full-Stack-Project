package com.nextflix.clone.service;

import com.nextflix.clone.dto.request.UserRequest;
import com.nextflix.clone.dto.response.MessageResponse;
import com.nextflix.clone.dto.response.PageResponse;
import com.nextflix.clone.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

public interface UserService {
    MessageResponse createUser(@Valid UserRequest userRequest);

    MessageResponse updateUser(Long id, UserRequest userRequest);

    PageResponse<UserResponse> getAllUsers(int page, int size, String search);

    MessageResponse deleteUser(Long id, String currentUserEmail);

    MessageResponse toggleUserStatus(Long id, String currentUserEmail);

    MessageResponse changeUserRole(Long id, UserRequest userRequest);
}
