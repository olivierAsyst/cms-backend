package anubis.lab.tumainiafricanews.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.server.url}")
    private String minioUrl;
    @Value("${minio.server.access-key}")
    private String minioAccessKey;
    @Value("${minio.server.secret-key}")
    private String minioSecretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }

    public String getMinioUrl() {
        return minioUrl;
    }

    public String getMinioAccessKey() {
        return minioAccessKey;
    }

    public String getMinioSecretKey() {
        return minioSecretKey;
    }
}
