package com.nextflix.clone.dto.request;

import com.nextflix.clone.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class UserRequest {
    private String email;
    private String password;
    private String FullName;
    private String role;
    private String active;
}
