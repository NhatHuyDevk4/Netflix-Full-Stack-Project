package com.nextflix.clone.controller;

import com.nextflix.clone.dto.request.UserRequest;
import com.nextflix.clone.dto.response.MessageResponse;
import com.nextflix.clone.dto.response.PageResponse;
import com.nextflix.clone.dto.response.UserResponse;
import com.nextflix.clone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')") // Anotation này đảm bảo rằng chỉ người dùng có vai trò ADMIN mới có thể truy cập các endpoint trong UserController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
        @RequestParam (defaultValue = "0") int page,
        @RequestParam (defaultValue = "10") int size,
        @RequestParam (required = false) String search
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, search));
    }


    @PostMapping("")
    public ResponseEntity<MessageResponse> createUser(
            @Valid
            @RequestBody UserRequest userRequest
            ) {
        return ResponseEntity.ok(
                userService.createUser(userRequest)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateUser(
            @PathVariable Long id,
            @Valid
            @RequestBody UserRequest userRequest
    ) {
        return ResponseEntity.ok(
                userService.updateUser(id, userRequest)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(
                userService.deleteUser(id, currentUserEmail)
        );
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<MessageResponse> toggleUserStatus(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();
        return ResponseEntity.ok(
                userService.toggleUserStatus(id, currentUserEmail)
        );
    }

    @PutMapping("/{id}/change-role")
    public ResponseEntity<MessageResponse> changeUserRole(
            @PathVariable Long id,
            @RequestBody UserRequest userRequest
    ) {
        return ResponseEntity.ok(
                userService.changeUserRole(id, userRequest)
        );
    }

}
