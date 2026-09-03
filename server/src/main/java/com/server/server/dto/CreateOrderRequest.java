package com.server.server.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {

    private Long userId;

    private Long petId;

    private Long prescriptionId;
}
