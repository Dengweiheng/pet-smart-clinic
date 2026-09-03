package com.server.server.dto;

import lombok.Data;

@Data
public class DrugInventoryUpdateRequest {

    private String drugCode;

    private Integer stock;
}
