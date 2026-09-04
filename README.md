# opensearch-template

A basic Spring Boot template for projects that use **OpenSearch**.

This project gives you a simple starting point with:
- Spring Boot (Web MVC + Data JPA)
- OpenSearch client integration
- PostgreSQL support
- Docker Compose setup for local OpenSearch and PostgreSQL

## What this project does

It provides a ready backend template where you can quickly build APIs that store relational data in PostgreSQL and index/search data in OpenSearch.

## Quick start

1. Start infrastructure:
   ```bash
   docker compose up -d
   ```
2. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
