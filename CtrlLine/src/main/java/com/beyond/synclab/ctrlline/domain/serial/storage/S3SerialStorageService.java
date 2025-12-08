package com.beyond.synclab.ctrlline.domain.serial.storage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.zip.GZIPInputStream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
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

        // ------------------------
        // 신규 업로드 (or 덮어쓰기 모드일 때는 조건 제거)
        // ------------------------
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType("application/gzip")
            .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(gzipPayload));

        log.info("Uploaded serial file to S3. key={}", key);

        return S3_PREFIX + bucket + "/" + key;
    }

    // -----------------------------------------------------------------------
    // 2) 읽기 (gzip 해제 포함)
    // -----------------------------------------------------------------------
    @Override
    public List<String> read(String path) {

        if (!path.startsWith(S3_PREFIX)) {
            throw new IllegalArgumentException("Invalid S3 path: " + path);
        }

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
    // 멱등성 보장: orderNo 기준으로 key 고정
    // -----------------------------------------------------------------------
    private String buildObjectKey(String orderNo) {
        String safeOrder = StringUtils.hasText(orderNo)
            ? orderNo.replaceAll("[^A-Za-z0-9_-]", "_")
            : "order";

        // timestamp 제거해서 멱등성 보장
        return basePath + "/" + safeOrder + "/serials.gz";
    }
}
