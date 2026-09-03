package com.server.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class PrescriptionCreateRequest {

    private String consultationId;

    private String vetId;

    private String vetName;

    private String petId;

    private String diagnosis;

    private Integer validDays = 3;

    private List<PrescriptionDrugItem> items;

    @Data
    public static class PrescriptionDrugItem {
        private String drugCode;
        private String drugName;
        private String dosage;
        private Integer quantity;
    }
}
