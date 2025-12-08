package com.beyond.synclab.ctrlline.domain.serial.storage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3SerialStorageService implements SerialStorageService {
    private static final String S3_PREFIX = "s3://";
    private final S3Client s3Client;
    private final String bucket;
    private final String basePath;

    public S3SerialStorageService(S3Client s3Client, String bucket, String basePath) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.basePath = basePath != null ? basePath : "serials";
    }

    // -----------------------------------------------------------------------
    // 1) GZIP 파일 저장
    // -----------------------------------------------------------------------
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
        return S3_PREFIX + bucket + "/" + key;
    }

    // -----------------------------------------------------------------------
    // 2) GZIP 파일 읽기
    // -----------------------------------------------------------------------
    @Override
    public List<String> read(String path) {

        // ★ 수정됨: 상수 사용
        if (!path.startsWith(S3_PREFIX)) {
            throw new IllegalArgumentException("Invalid S3 path: " + path);
        }

        // ★ 수정됨: 상수 사용
        String withoutPrefix = path.substring(S3_PREFIX.length());

        int slash = withoutPrefix.indexOf("/");
        String bucketName = withoutPrefix.substring(0, slash);
        String key = withoutPrefix.substring(slash + 1);

        try (ResponseInputStream<?> s3Stream = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
             GZIPInputStream gzipInputStream = new GZIPInputStream(s3Stream);
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream))
        ) {
            return reader.lines().toList();

        } catch (Exception ex) {
            throw new IllegalStateException("S3 시리얼 파일 읽기 실패: " + path, ex);
        }
    }

    // -----------------------------------------------------------------------
    // 내부 메서드 - Object Key 생성
    // -----------------------------------------------------------------------
    private String buildObjectKey(String orderNo) {
        String safeOrder = StringUtils.hasText(orderNo)
                ? orderNo.replaceAll("[^A-Za-z0-9_-]", "_")
                : "order";

        String prefix = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;

        return prefix + "/" + safeOrder + "/serials-" + Instant.now().toEpochMilli() + ".gz";
    }
}
