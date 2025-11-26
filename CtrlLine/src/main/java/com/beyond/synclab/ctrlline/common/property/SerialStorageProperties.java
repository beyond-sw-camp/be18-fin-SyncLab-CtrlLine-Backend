package com.beyond.synclab.ctrlline.common.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ToString
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.serial-storage")
public class SerialStorageProperties {

    private final StorageType type;
    private final Local local;
    private final S3 s3;

    public enum StorageType {
        LOCAL,
        S3
    }

    public record Local(String baseDir) {
    }

    public record S3(String bucket, String basePath, String region) {
    }
}
