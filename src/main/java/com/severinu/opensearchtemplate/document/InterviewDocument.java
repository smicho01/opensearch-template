package com.severinu.opensearchtemplate.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewDocument {

    private String id;
    private String participantName;
    private String location;
    private String country;
    private String interviewDate;
    private String uploadDateTime;
    private List<QAndA> transcript;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QAndA {
        private String question;
        private String answer;
    }
}