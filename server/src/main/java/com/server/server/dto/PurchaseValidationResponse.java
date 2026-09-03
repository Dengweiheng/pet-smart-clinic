package com.server.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseValidationResponse {

    private boolean pass;

    private String message;

    private boolean prescriptionRequired;

    private List<String> rejectedDrugCodes;
}
