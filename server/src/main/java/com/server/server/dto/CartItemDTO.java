package com.server.server.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long id;
    private Long drugId;
    private String drugName;
    private String drugCode;
    private BigDecimal unitPrice;
    private Integer quantity;
    private Integer isRx;
}
