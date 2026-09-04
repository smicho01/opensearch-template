package com.severinu.opensearchtemplate.service;

import com.severinu.opensearchtemplate.document.InterviewDocument;
import com.severinu.opensearchtemplate.dto.SearchRequest;
import com.severinu.opensearchtemplate.dto.SearchResponse;
import com.severinu.opensearchtemplate.dto.SearchResult;
import com.severinu.opensearchtemplate.repository.OpenSearchRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InterviewSearchService {

    private final OpenSearchRepository openSearchRepository;
    private final Clock clock;

    public InterviewSearchService(OpenSearchRepository openSearchRepository, Clock clock) {
        this.openSearchRepository = openSearchRepository;
        this.clock = clock;
    }

    public void indexInterview(InterviewDocument document) throws IOException {
        document.setUploadDateTime(
                LocalDateTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        openSearchRepository.indexDocument(document);
    }

    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        var response = openSearchRepository.search(searchRequest);
        List<SearchResult> results = openSearchRepository.mapSearchResults(response);

        return new SearchResponse(
                results,
                response.getHits().getTotalHits().value(),
                searchRequest.getPage(),
                searchRequest.getSize()
        );
    }

    public void deleteInterview(String id) throws IOException {
        openSearchRepository.deleteDocument(id);
    }


}