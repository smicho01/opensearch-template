package com.severinu.opensearchtemplate.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.severinu.opensearchtemplate.document.InterviewDocument;
import com.severinu.opensearchtemplate.dto.SearchRequest;
import com.severinu.opensearchtemplate.dto.SearchResult;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class OpenSearchRepository {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;

    @Value("${opensearch.index-name:interviews}")
    private String indexName;

    public OpenSearchRepository(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public void indexDocument(InterviewDocument document) throws IOException {
        IndexRequest request = new IndexRequest(indexName)
                .id(document.getId())
                .source(objectMapper.writeValueAsString(document), XContentType.JSON);

        client.index(request, RequestOptions.DEFAULT);
    }

    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        org.opensearch.action.search.SearchRequest request =
                new org.opensearch.action.search.SearchRequest(indexName);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(buildQuery(searchRequest));
        sourceBuilder.from(searchRequest.getPage() * searchRequest.getSize());
        sourceBuilder.size(searchRequest.getSize());
        sourceBuilder.trackTotalHits(true);

        if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().isBlank()) {
            sourceBuilder.sort(searchRequest.getSortBy(), SortOrder.ASC);
        } else {
            sourceBuilder.sort("uploadDateTime", SortOrder.DESC);
        }

        request.source(sourceBuilder);
        return client.search(request, RequestOptions.DEFAULT);
    }

    public void deleteDocument(String id) throws IOException {
        DeleteRequest request = new DeleteRequest(indexName, id);
        client.delete(request, RequestOptions.DEFAULT);
    }

    private BoolQueryBuilder buildQuery(SearchRequest searchRequest) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (!searchRequest.hasRelevantCriteria()) {
            boolQuery.must(QueryBuilders.matchAllQuery());
            return boolQuery;
        }

        if (searchRequest.getContent() != null) {
            for (String term : searchRequest.getContent()) {
                if (term != null && !term.isBlank()) {
                    boolQuery.must(QueryBuilders.multiMatchQuery(term,
                            "transcript.answer",
                            "transcript.question"
                    ));
                }
            }
        }

        if (searchRequest.getMetadata() != null) {
            for (Map.Entry<String, String> entry : searchRequest.getMetadata().entrySet()) {
                boolQuery.must(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
            }
        }

        return boolQuery;
    }

    public List<SearchResult> mapSearchResults(SearchResponse response) {
        List<SearchResult> results = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            SearchResult result = new SearchResult();
            result.setFileId(hit.getId());
            result.setParticipantName((String) source.get("participantName"));
            result.setCountry((String) source.get("country"));
            result.setLocation((String) source.get("location"));
            result.setInterviewDate((String) source.get("interviewDate"));
            results.add(result);
        }
        return results;
    }
}