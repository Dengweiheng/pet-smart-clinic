package com.server.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseValidationRequest {

    private String petId;

    private String prescriptionId;

    private List<OrderDrugItem> items;

    @Data
    public static class OrderDrugItem {
        private String drugCode;
        private Integer quantity;
    }
}
