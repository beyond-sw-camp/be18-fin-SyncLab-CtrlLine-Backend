package com.beyond.synclab.ctrlline.config;

import com.beyond.synclab.ctrlline.common.property.SerialStorageProperties;
import com.beyond.synclab.ctrlline.common.property.SerialStorageProperties.StorageType;
import com.beyond.synclab.ctrlline.domain.serial.storage.LocalSerialStorageService;
import com.beyond.synclab.ctrlline.domain.serial.storage.S3SerialStorageService;
import com.beyond.synclab.ctrlline.domain.serial.storage.SerialStorageService;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class SerialStorageConfig {

    private final SerialStorageProperties properties;

    @Bean
    public SerialStorageService serialStorageService(Optional<S3Client> s3ClientOptional) {
        StorageType storageType = properties.getType() != null ? properties.getType() : StorageType.LOCAL;
        if (storageType == StorageType.S3) {
            SerialStorageProperties.S3 s3 = properties.getS3();
            if (s3 == null || isBlank(s3.bucket()) || isBlank(s3.basePath()) || isBlank(s3.region())) {
                throw new IllegalStateException("S3 storage configuration requires bucket, basePath, and region");
            }
            S3Client client = s3ClientOptional.orElseThrow(() ->
                    new IllegalStateException("S3 client is not configured for serial storage"));
            return new S3SerialStorageService(client, s3.bucket(), s3.basePath());
        }
        SerialStorageProperties.Local local = properties.getLocal();
        if (local == null || local.baseDir() == null) {
            throw new IllegalStateException("Local storage configuration requires baseDir");
        }
        return new LocalSerialStorageService(Path.of(local.baseDir()));
    }

    @Bean
    @ConditionalOnProperty(name = "app.serial-storage.type", havingValue = "s3")
    public S3Client s3Client() {
        SerialStorageProperties.S3 s3 = properties.getS3();
        if (s3 == null || isBlank(s3.region())) {
            throw new IllegalStateException("S3 storage configuration requires region");
        }
        return S3Client.builder()
                .region(Region.of(s3.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
