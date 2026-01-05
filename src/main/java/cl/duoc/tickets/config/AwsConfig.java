package cl.duoc.tickets.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    /**
     * Si existe, usamos LocalStack (o endpoint custom).
     * Ejemplo: http://localhost:4566
     */
    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;

    /**
     * Para LocalStack normalmente debe ser true (path-style).
     */
    @Value("${aws.s3.path-style:true}")
    private boolean pathStyle;

    @Bean
    public S3Client s3Client() {

        // Usar 'var' evita el error del Language Server con S3Client.Builder
        var builder = S3Client.builder()
                .region(Region.of(region))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(pathStyle)
                                .build()
                );

        // LocalStack / endpoint custom
        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            builder = builder
                    .endpointOverride(URI.create(s3Endpoint))
                    // LocalStack acepta credenciales dummy
                    .credentialsProvider(
                            StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"))
                    );
        } else {
            // AWS real: toma credenciales desde environment / profile / IAM role, etc.
            builder = builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
