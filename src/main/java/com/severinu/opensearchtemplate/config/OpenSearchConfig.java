package com.severinu.opensearchtemplate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Configuration
public class OpenSearchConfig {

    @Value("${opensearch.host}")
    private String host;

    @Value("${opensearch.port}")
    private int port;

    @Value("${opensearch.scheme}")
    private String scheme;

    @Value("${opensearch.username}")
    private String username;

    @Value("${opensearch.password}")
    private String password;

//    @Bean
//    public RestHighLevelClient openSearchClient() throws Exception {
//        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(
//                new AuthScope(host, port),
//                new UsernamePasswordCredentials(username, password.toCharArray())
//        );
//
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
//            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
//            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
//            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
//        }}, new java.security.SecureRandom());
//
//        TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
//                .setSslContext(sslContext)
//                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
//                .build();
//
//        PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
//                .setTlsStrategy(tlsStrategy)
//                .build();
//
//        RestClientBuilder builder = RestClient.builder(new HttpHost(scheme, host, port))
//                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
//                        .setDefaultCredentialsProvider(credentialsProvider)
//                        .setConnectionManager(connectionManager)
//                );
//
//        return new RestHighLevelClient(builder);
//    }

    @Bean
    public RestHighLevelClient openSearchClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(scheme, host, port));
        return new RestHighLevelClient(builder);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}