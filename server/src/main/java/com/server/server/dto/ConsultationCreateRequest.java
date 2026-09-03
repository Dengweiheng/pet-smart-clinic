package com.server.server.dto;

import lombok.Data;

@Data
public class ConsultationCreateRequest {

    private String userId;

    private String petId;

    private String petName;

    private String problemDescription;
}
