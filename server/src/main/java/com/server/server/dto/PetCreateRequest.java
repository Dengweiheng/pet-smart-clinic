package com.server.server.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PetCreateRequest {

    private String ownerUserId;

    private String name;

    private String species;

    private String breed;

    private String gender;

    private String birthDate;

    private BigDecimal weightKg;
}
