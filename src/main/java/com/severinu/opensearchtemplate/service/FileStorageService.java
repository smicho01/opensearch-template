package com.severinu.opensearchtemplate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    public String uploadToS3(String fileName) {
        log.info("DUMMY: uploading {} to S3", fileName);
        return "s3://interview-bucket/" + fileName;
    }

    public void deleteFromS3(String s3Key) {
        log.info("DUMMY: deleting {} from S3", s3Key);
    }

    public void saveMetadataToPostgres(String fileId, String fileName) {
        log.info("DUMMY: saving metadata for file {} to Postgres", fileId);
    }

    public void deleteMetadataFromPostgres(String fileId) {
        log.info("DUMMY: deleting metadata for file {} from Postgres", fileId);
    }
}