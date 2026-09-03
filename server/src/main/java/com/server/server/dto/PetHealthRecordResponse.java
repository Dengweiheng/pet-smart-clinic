package com.server.server.dto;

import lombok.Data;

@Data
public class PetHealthRecordResponse {

    private String petId;

    private String allergies;

    private String chronicDiseases;

    private String vaccineNotes;

    private String medicationNotes;
}
