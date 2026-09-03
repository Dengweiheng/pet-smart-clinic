package com.server.server.dto;

import lombok.Data;

@Data
public class ShipOrderRequest {

    private String orderNo;

    private String companyName;

    private String trackingNo;
}
