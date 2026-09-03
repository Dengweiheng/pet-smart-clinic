package com.server.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class HealthConsultationRequest {

    private String petName;

    private String species;

    private String breed;

    private String age;

    private String symptoms;

    private List<HistoryMessage> history;

    @Data
    public static class HistoryMessage {
        private String role;
        private String content;
    }
}
