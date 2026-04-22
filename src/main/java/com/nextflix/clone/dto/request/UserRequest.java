package com.nextflix.clone.dto.request;

import lombok.Data;

@Data
public class UserRequest {
    private String email;
    private String password;
    private String FullName;
    private String role;
    private String active;
}
