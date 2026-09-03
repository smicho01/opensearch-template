package com.severinu.opensearchtemplate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.severinu.opensearchtemplate.document.InterviewDocument;
import com.severinu.opensearchtemplate.dto.SearchRequest;
import com.severinu.opensearchtemplate.dto.SearchResponse;
import com.severinu.opensearchtemplate.dto.UploadResponse;
import com.severinu.opensearchtemplate.service.FileStorageService;
import com.severinu.opensearchtemplate.service.InterviewSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewSearchService interviewSearchService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Value("${opensearch.pagination.default-size:10}")
    private int defaultPageSize;

    public InterviewController(InterviewSearchService interviewSearchService,
                               FileStorageService fileStorageService,
                               ObjectMapper objectMapper) {
        this.interviewSearchService = interviewSearchService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<UploadResponse> uploadInterview(@RequestBody InterviewDocument document) throws IOException {
        String id = UUID.randomUUID().toString();
        document.setId(id);

        fileStorageService.saveMetadataToPostgres(id, document.getParticipantName());
        fileStorageService.uploadToS3(document.getParticipantName() + ".pdf");
        interviewSearchService.indexInterview(document);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadResponse(id, "CREATED"));
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestBody Map<String, Object> searchPayload) throws IOException {
        if (searchPayload.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SearchRequest searchRequest = objectMapper.convertValue(searchPayload, SearchRequest.class);

        if (!searchRequest.hasCriteria()) {
            return ResponseEntity.badRequest().build();
        }

        if (searchRequest.getSize() <= 0) {
            searchRequest.setSize(defaultPageSize);
        }

        if (searchRequest.getPage() < 0) {
            searchRequest.setPage(0);
        }

        SearchResponse response = interviewSearchService.search(searchRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable String id) throws IOException {
        interviewSearchService.deleteInterview(id);
        fileStorageService.deleteFromS3(id);
        fileStorageService.deleteMetadataFromPostgres(id);

        return ResponseEntity.noContent().build();
    }
}