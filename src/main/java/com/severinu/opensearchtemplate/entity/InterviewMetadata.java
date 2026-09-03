package com.severinu.opensearchtemplate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interview_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String participantName;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private LocalDate interviewDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}