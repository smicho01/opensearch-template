package com.severinu.opensearchtemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String fileId;
    private String participantName;
    private String country;
    private String location;
    private String interviewDate;
}