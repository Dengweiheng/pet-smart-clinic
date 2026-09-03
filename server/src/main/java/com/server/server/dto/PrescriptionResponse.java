package com.server.server.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionResponse {

    private String prescriptionId;

    private String consultationId;

    private String vetId;

    private String vetName;

    private String petId;

    private String diagnosis;

    private String status;

    private LocalDateTime issuedAt;

    private LocalDateTime expiresAt;

    private List<PrescriptionDrug> items;

    @Data
    public static class PrescriptionDrug {
        private String drugCode;
        private String drugName;
        private String dosage;
        private Integer quantity;
    }
}
