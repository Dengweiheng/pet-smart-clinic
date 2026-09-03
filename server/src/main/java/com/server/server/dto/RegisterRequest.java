package com.server.server.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String phone;
    private String role; // USER, VET, PHARMACIST
}
