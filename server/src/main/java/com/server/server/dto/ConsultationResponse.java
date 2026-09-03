package com.server.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationResponse {

    private String consultationId;

    private String userId;

    private String petId;

    private String petName;

    private String problemDescription;

    private String status;

    private LocalDateTime createdAt;
}
