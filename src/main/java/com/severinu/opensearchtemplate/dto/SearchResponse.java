package com.severinu.opensearchtemplate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    private List<SearchResult> documents;
    private long totalResults;
    private int page;
    private int size;
}