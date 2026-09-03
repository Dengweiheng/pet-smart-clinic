package com.server.server.dto;

import lombok.Data;

@Data
public class PetHealthRecordRequest {

    private String allergies;

    private String chronicDiseases;

    private String vaccineNotes;

    private String medicationNotes;
}
