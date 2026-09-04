package com.severinu.opensearchtemplate.controller;

import com.severinu.opensearchtemplate.document.InterviewDocument;
import com.severinu.opensearchtemplate.dto.SearchRequest;
import com.severinu.opensearchtemplate.dto.SearchResponse;
import com.severinu.opensearchtemplate.dto.UploadResponse;
import com.severinu.opensearchtemplate.service.FileStorageService;
import com.severinu.opensearchtemplate.service.InterviewSearchService;
import com.severinu.opensearchtemplate.service.SearchRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewSearchService interviewSearchService;
    private final FileStorageService fileStorageService;
    private final SearchRequestService searchRequestService;

    public InterviewController(InterviewSearchService interviewSearchService,
                               FileStorageService fileStorageService,
                               SearchRequestService searchRequestService) {
        this.interviewSearchService = interviewSearchService;
        this.fileStorageService = fileStorageService;
        this.searchRequestService = searchRequestService;
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
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest searchRequest) throws IOException {
        SearchRequest normalizedSearchRequest;
        try {
            normalizedSearchRequest = searchRequestService.normalizeAndValidate(searchRequest);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

        SearchResponse response = interviewSearchService.search(normalizedSearchRequest);
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