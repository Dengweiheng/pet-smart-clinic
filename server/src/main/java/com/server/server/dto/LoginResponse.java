package com.server.server.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private Long userId;

    private String token;

    private String username;

    private String role;
}
