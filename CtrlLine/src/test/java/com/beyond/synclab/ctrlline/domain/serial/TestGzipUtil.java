package com.beyond.synclab.ctrlline.domain.serial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * 테스트 환경에서 gzip 데이터를 생성하기 위한 유틸리티.
 */
public final class TestGzipUtil {

    // private 생성자로 인스턴스화 방지
    private TestGzipUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static byte[] gzip(String content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(content.getBytes());
        }

        return bos.toByteArray();
    }
}
