package com.severinu.opensearchtemplate.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchRequest {
    private List<String> content;
    private Map<String, String> metadata;
    private int page;
    private int size;
    private String sortBy;

    public boolean hasCriteria() {
        return content != null || metadata != null || size > 0 || page > 0 || sortBy != null;
    }

    public boolean hasRelevantCriteria() {
        boolean hasContent = content != null && !content.isEmpty()
                && content.stream().anyMatch(c -> c != null && !c.isBlank());
        boolean hasMetadata = metadata != null && !metadata.isEmpty();
        return hasContent || hasMetadata;
    }
}