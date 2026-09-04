package com.severinu.opensearchtemplate.service;

import com.severinu.opensearchtemplate.dto.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SearchRequestService {

    private final int defaultPageSize;

    public SearchRequestService(@Value("${opensearch.pagination.default-size:10}") int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public SearchRequest normalizeAndValidate(SearchRequest searchRequest) {
        if (searchRequest == null || !searchRequest.hasCriteria()) {
            throw new IllegalArgumentException("At least one search criterion is required.");
        }

        if (searchRequest.getSize() <= 0) {
            searchRequest.setSize(defaultPageSize);
        }

        if (searchRequest.getPage() < 0) {
            searchRequest.setPage(0);
        }

        return searchRequest;
    }
}
