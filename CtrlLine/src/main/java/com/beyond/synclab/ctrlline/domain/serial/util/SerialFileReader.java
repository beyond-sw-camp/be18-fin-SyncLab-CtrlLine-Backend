// file: domain/serial/util/SerialFileReader.java
package com.beyond.synclab.ctrlline.domain.serial.util;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Slf4j
public class SerialFileReader {

    // Local 또는 S3 경로에서 gzip 파일을 읽어
    // 시리얼 번호 리스트를 반환한다.
    public static List<String> readSerialFile(String path, S3Client s3Client) {

        try (InputStream is = openStream(path, s3Client);
             GZIPInputStream gis = new GZIPInputStream(is);
             BufferedReader br = new BufferedReader(new InputStreamReader(gis))
        ) {
            return br.lines().toList();

        } catch (Exception ex) {
            log.error("Failed to read serial file. path={}", path, ex);
            throw new IllegalStateException("시리얼 파일 읽기 실패: " + path);
        }
    }

    // Local file 또는 S3 파일을 읽는 스트림 생성
    private static InputStream openStream(String path, S3Client s3Client) throws IOException {

        if (path.startsWith("s3://")) {
            // s3://bucket/key 형식 파싱
            String noPrefix = path.substring("s3://".length());
            int slash = noPrefix.indexOf("/");
            String bucket = noPrefix.substring(0, slash);
            String key = noPrefix.substring(slash + 1);

            ResponseInputStream<?> s3is = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            return s3is;
        }

        // Local file
        return Files.newInputStream(Path.of(path));
    }
}
