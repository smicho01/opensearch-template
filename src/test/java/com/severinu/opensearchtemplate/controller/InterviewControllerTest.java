package com.severinu.opensearchtemplate.controller;

import com.severinu.opensearchtemplate.document.InterviewDocument;
import com.severinu.opensearchtemplate.dto.SearchRequest;
import com.severinu.opensearchtemplate.dto.SearchResponse;
import com.severinu.opensearchtemplate.dto.SearchResult;
import com.severinu.opensearchtemplate.dto.UploadResponse;
import com.severinu.opensearchtemplate.service.FileStorageService;
import com.severinu.opensearchtemplate.service.InterviewSearchService;
import com.severinu.opensearchtemplate.service.SearchRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewControllerTest {

    private static final String PARTICIPANT_NAME = "Jane Doe";
    private static final String PARTICIPANT_FILE_NAME = PARTICIPANT_NAME + ".pdf";
    private static final String INTERVIEW_ID = "abc-123";

    @Mock
    private InterviewSearchService interviewSearchService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private SearchRequestService searchRequestService;

    private InterviewController interviewController;

    @BeforeEach
    void setUp() {
        interviewController = new InterviewController(
                interviewSearchService,
                fileStorageService,
                searchRequestService
        );
    }

    @Test
    void uploadInterviewShouldReturnCreatedAndPersistUsingGeneratedId() throws IOException {
        InterviewDocument document = createInterviewDocument();

        ResponseEntity<UploadResponse> response = interviewController.uploadInterview(document);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CREATED", response.getBody().getStatus());

        String generatedId = response.getBody().getFileId();

        assertNotNull(generatedId);
        UUID.fromString(generatedId);
        assertEquals(generatedId, document.getId());

        verify(fileStorageService).saveMetadataToPostgres(generatedId, PARTICIPANT_NAME);
        verify(fileStorageService).uploadToS3(PARTICIPANT_FILE_NAME);
        verify(interviewSearchService).indexInterview(document);
        verifyNoMoreInteractions(fileStorageService, interviewSearchService);
    }

    @Test
    void uploadInterviewShouldPropagateWhenMetadataSaveFails() {
        InterviewDocument document = createInterviewDocument();
        RuntimeException expectedException = new RuntimeException("metadata failure");

        doThrow(expectedException)
                .when(fileStorageService)
                .saveMetadataToPostgres(anyString(), eq(PARTICIPANT_NAME));

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> interviewController.uploadInterview(document)
        );

        assertSame(expectedException, thrown);

        verify(fileStorageService)
                .saveMetadataToPostgres(anyString(), eq(PARTICIPANT_NAME));
        verifyNoMoreInteractions(fileStorageService);
        verifyNoInteractions(interviewSearchService);
    }

    @Test
    void uploadInterviewShouldPropagateIOExceptionWhenIndexingFails() throws IOException {
        InterviewDocument document = createInterviewDocument();
        IOException expectedException = new IOException("index failure");

        doThrow(expectedException)
                .when(interviewSearchService)
                .indexInterview(document);

        IOException thrown = assertThrows(
                IOException.class,
                () -> interviewController.uploadInterview(document)
        );

        assertSame(expectedException, thrown);

        verify(fileStorageService)
                .saveMetadataToPostgres(anyString(), eq(PARTICIPANT_NAME));
        verify(fileStorageService).uploadToS3(PARTICIPANT_FILE_NAME);
        verify(interviewSearchService).indexInterview(document);
        verifyNoMoreInteractions(fileStorageService, interviewSearchService);
    }

    @Test
    void uploadInterviewShouldPropagateWhenS3UploadFails() {
        InterviewDocument document = createInterviewDocument();
        RuntimeException expectedException = new RuntimeException("s3 upload failure");

        doThrow(expectedException)
                .when(fileStorageService)
                .uploadToS3(PARTICIPANT_FILE_NAME);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> interviewController.uploadInterview(document)
        );

        assertSame(expectedException, thrown);

        verify(fileStorageService)
                .saveMetadataToPostgres(anyString(), eq(PARTICIPANT_NAME));
        verify(fileStorageService).uploadToS3(PARTICIPANT_FILE_NAME);
        verifyNoMoreInteractions(fileStorageService);
        verifyNoInteractions(interviewSearchService);
    }

    @Test
    void searchShouldReturnBadRequestWhenNormalizationFails() throws IOException {
        SearchRequest request = new SearchRequest();

        when(searchRequestService.normalizeAndValidate(request))
                .thenThrow(new IllegalArgumentException("invalid criteria"));

        ResponseEntity<SearchResponse> response = interviewController.search(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(searchRequestService).normalizeAndValidate(request);
        verifyNoInteractions(interviewSearchService);
    }

    @Test
    void searchShouldReturnBadRequestWhenNullRequestIsRejected() throws IOException {
        when(searchRequestService.normalizeAndValidate(null))
                .thenThrow(new IllegalArgumentException("invalid criteria"));

        ResponseEntity<SearchResponse> response = interviewController.search(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(searchRequestService).normalizeAndValidate(null);
        verifyNoInteractions(interviewSearchService);
    }

    @Test
    void searchShouldUseNormalizedRequestAndReturnOk() throws IOException {
        SearchRequest incomingRequest = new SearchRequest();

        SearchRequest normalizedRequest = new SearchRequest();
        normalizedRequest.setSize(10);
        normalizedRequest.setPage(0);
        normalizedRequest.setContent(List.of("java"));

        SearchResponse expected = new SearchResponse(
                List.of(new SearchResult(
                        "id-1",
                        PARTICIPANT_NAME,
                        "RO",
                        "Cluj",
                        "2026-01-01"
                )),
                1L,
                0,
                10
        );

        when(searchRequestService.normalizeAndValidate(incomingRequest))
                .thenReturn(normalizedRequest);
        when(interviewSearchService.search(normalizedRequest))
                .thenReturn(expected);

        ResponseEntity<SearchResponse> response = interviewController.search(incomingRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());

        verify(searchRequestService).normalizeAndValidate(incomingRequest);
        verify(interviewSearchService).search(normalizedRequest);
        verifyNoMoreInteractions(searchRequestService, interviewSearchService);
    }

    @Test
    void searchShouldPropagateIOExceptionFromSearchService() throws IOException {
        SearchRequest incomingRequest = new SearchRequest();
        SearchRequest normalizedRequest = new SearchRequest();
        IOException expectedException = new IOException("search failure");

        when(searchRequestService.normalizeAndValidate(incomingRequest))
                .thenReturn(normalizedRequest);
        when(interviewSearchService.search(normalizedRequest))
                .thenThrow(expectedException);

        IOException thrown = assertThrows(
                IOException.class,
                () -> interviewController.search(incomingRequest)
        );

        assertSame(expectedException, thrown);

        verify(searchRequestService).normalizeAndValidate(incomingRequest);
        verify(interviewSearchService).search(normalizedRequest);
        verifyNoMoreInteractions(searchRequestService, interviewSearchService);
    }

    @Test
    void deleteInterviewShouldReturnNoContentAndDeleteInOrder() throws IOException {
        ResponseEntity<Void> response = interviewController.deleteInterview(INTERVIEW_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        InOrder inOrder = inOrder(interviewSearchService, fileStorageService);

        inOrder.verify(interviewSearchService).deleteInterview(INTERVIEW_ID);
        inOrder.verify(fileStorageService).deleteFromS3(INTERVIEW_ID);
        inOrder.verify(fileStorageService).deleteMetadataFromPostgres(INTERVIEW_ID);

        verifyNoMoreInteractions(interviewSearchService, fileStorageService);
    }

    @Test
    void deleteInterviewShouldPropagateWhenOpenSearchDeleteFails() throws IOException {
        IOException expectedException = new IOException("delete failure");

        doThrow(expectedException)
                .when(interviewSearchService)
                .deleteInterview(INTERVIEW_ID);

        IOException thrown = assertThrows(
                IOException.class,
                () -> interviewController.deleteInterview(INTERVIEW_ID)
        );

        assertSame(expectedException, thrown);

        verify(interviewSearchService).deleteInterview(INTERVIEW_ID);
        verifyNoMoreInteractions(interviewSearchService);
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void deleteInterviewShouldStopWhenS3DeleteFails() throws IOException {
        RuntimeException expectedException = new RuntimeException("s3 failure");

        doThrow(expectedException)
                .when(fileStorageService)
                .deleteFromS3(INTERVIEW_ID);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> interviewController.deleteInterview(INTERVIEW_ID)
        );

        assertSame(expectedException, thrown);

        verify(interviewSearchService).deleteInterview(INTERVIEW_ID);
        verify(fileStorageService).deleteFromS3(INTERVIEW_ID);
        verify(fileStorageService, never()).deleteMetadataFromPostgres(anyString());
    }

    @Test
    void deleteInterviewShouldPropagateWhenMetadataDeleteFails() throws IOException {
        RuntimeException expectedException =
                new RuntimeException("metadata delete failure");

        doThrow(expectedException)
                .when(fileStorageService)
                .deleteMetadataFromPostgres(INTERVIEW_ID);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> interviewController.deleteInterview(INTERVIEW_ID)
        );

        assertSame(expectedException, thrown);

        verify(interviewSearchService).deleteInterview(INTERVIEW_ID);
        verify(fileStorageService).deleteFromS3(INTERVIEW_ID);
        verify(fileStorageService).deleteMetadataFromPostgres(INTERVIEW_ID);
        verifyNoMoreInteractions(interviewSearchService, fileStorageService);
    }

    private InterviewDocument createInterviewDocument() {
        InterviewDocument document = new InterviewDocument();
        document.setParticipantName(PARTICIPANT_NAME);
        return document;
    }
}