// file: domain/serial/util/SerialFileReader.java
package com.beyond.synclab.ctrlline.domain.serial.util;

import com.beyond.synclab.ctrlline.domain.serial.storage.SerialStorageService;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class SerialFileReader {

    // Local 또는 S3 경로에서 gzip 파일을 읽어
    // 시리얼 번호 리스트를 반환한다.
    private SerialFileReader() {
        throw new IllegalStateException("Utility class");
    }

    // Local file 또는 S3 파일을 읽는 스트림 생성
    public static List<String> readSerialFile(String path, SerialStorageService storageService) {
        try {
            return storageService.read(path);
        } catch (Exception ex) {
            log.error("Failed to read serial file. path={}", path, ex);
            throw new IllegalStateException("시리얼 파일 읽기 실패: " + path, ex);
        }
    }
}

