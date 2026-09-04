package com.severinu.opensearchtemplate.service;

import com.severinu.opensearchtemplate.document.InterviewDocument;
import com.severinu.opensearchtemplate.repository.OpenSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InterviewSearchServiceTest {

    @Mock
    private OpenSearchRepository openSearchRepository;

    private Clock clock;

    private InterviewSearchService interviewSearchService;

    @BeforeEach
    void setup() {
        clock = Clock.fixed(
                Instant.parse("2026-09-04T12:00:00Z"),
                ZoneOffset.UTC
        );
        interviewSearchService = new InterviewSearchService(openSearchRepository, clock);
    }

    @Test
    void deleteInterviewShouldDeleteDocumentById() throws IOException {
        String interviewId = "abc-123";

        interviewSearchService.deleteInterview(interviewId);

        verify(openSearchRepository).deleteDocument(interviewId);
    }

    @Test
    void deleteInterviewShouldPropagateIOException () throws IOException {
        String interviewId = "abc-123";
        IOException exception = new IOException("OpenSearch delete failed");

        doThrow(exception)
                .when(openSearchRepository)
                .deleteDocument(interviewId);

        assertThrows(
                IOException.class,
                () -> interviewSearchService.deleteInterview(interviewId)
        );
    }

    @Test
    void indexInterviewShouldSetUploadDateTimeAndIndexDocument () throws IOException {
        InterviewDocument document = new InterviewDocument();

        interviewSearchService.indexInterview(document);

        assertEquals(
                "2026-09-04T12:00:00",
                document.getUploadDateTime()
        );

        verify(openSearchRepository).indexDocument(document);
    }

    @Test
    void indexInterviewShouldPropagateIOException () throws IOException {
        InterviewDocument document = new InterviewDocument();
        IOException expectedException = new IOException("Indexing failed");

        doThrow(expectedException)
                .when(openSearchRepository)
                .indexDocument(document);

        assertThrows(
                IOException.class,
                () -> openSearchRepository.indexDocument(document)
        );
    }

}