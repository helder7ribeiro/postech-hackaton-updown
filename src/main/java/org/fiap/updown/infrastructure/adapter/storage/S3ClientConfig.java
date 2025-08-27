package org.fiap.updown.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.infrastructure.adapter.storage.S3StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3StorageProperties.class)
@RequiredArgsConstructor
public class S3ClientConfig {

    private final S3StorageProperties props;

    @Bean
    public S3Client s3Client() {
        S3Configuration s3Conf = S3Configuration.builder()
                .pathStyleAccessEnabled(props.isPathStyle()) // LocalStack precisa disso
                .build();

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())))
                .serviceConfiguration(s3Conf);

        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder = builder.endpointOverride(URI.create(props.getEndpoint())); // ex.: http://localstack:4566
        }

        return builder.build();
    }
}
