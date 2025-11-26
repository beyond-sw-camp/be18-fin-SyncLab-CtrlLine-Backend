package com.beyond.synclab.ctrlline.domain.serial.storage;

import java.time.Instant;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3SerialStorageService implements SerialStorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String basePath;

    public S3SerialStorageService(S3Client s3Client, String bucket, String basePath) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.basePath = basePath != null ? basePath : "serials";
    }

    @Override
    public String store(String orderNo, byte[] gzipPayload) {
        if (gzipPayload == null || gzipPayload.length == 0) {
            throw new IllegalArgumentException("Serial payload is empty");
        }
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("S3 bucket is not configured");
        }
        String key = buildObjectKey(orderNo);
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/gzip")
                .build();
        s3Client.putObject(putRequest, RequestBody.fromBytes(gzipPayload));
        return "s3://" + bucket + "/" + key;
    }

    private String buildObjectKey(String orderNo) {
        String safeOrder = StringUtils.hasText(orderNo) ? orderNo.replaceAll("[^A-Za-z0-9_-]", "_") : "order";
        String prefix = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
        return prefix + "/" + safeOrder + "/serials-" + Instant.now().toEpochMilli() + ".gz";
    }
}
