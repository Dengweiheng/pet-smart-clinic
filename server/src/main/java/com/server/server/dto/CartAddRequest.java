package com.server.server.dto;

import lombok.Data;

@Data
public class CartAddRequest {

    private Long userId;

    private Long petId;

    private Long drugId;

    private Integer quantity;
}
