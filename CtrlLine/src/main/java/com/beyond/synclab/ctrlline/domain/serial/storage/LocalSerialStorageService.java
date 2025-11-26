package com.beyond.synclab.ctrlline.domain.serial.storage;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class LocalSerialStorageService implements SerialStorageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Path FALLBACK_BASE_DIR = Path.of(
            System.getProperty("java.io.tmpdir"),
            "ctrlline",
            "serials"
    );

    private final Path baseDir;

    public LocalSerialStorageService(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public String store(String orderNo, byte[] gzipPayload) {
        if (gzipPayload == null || gzipPayload.length == 0) {
            throw new IllegalArgumentException("Serial payload is empty");
        }
        String safeOrderNo = sanitize(orderNo);
        try {
            String dateSegment = DATE_FORMATTER.format(LocalDate.now());
            Path directory = resolveStorageDirectory(dateSegment);
            Path target = directory.resolve(safeOrderNo + "-serials.gz");
            Files.write(target, gzipPayload, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store serial payload locally", ex);
        }
    }

    private Path resolveStorageDirectory(String dateSegment) throws IOException {
        Path primaryDirectory = baseDir.resolve(dateSegment);
        try {
            Files.createDirectories(primaryDirectory);
            return primaryDirectory;
        } catch (AccessDeniedException ex) {
            Path fallbackDirectory = FALLBACK_BASE_DIR.resolve(dateSegment);
            Files.createDirectories(fallbackDirectory);
            log.warn("시리얼 파일을 기본 경로({})에 저장할 수 없어 임시 경로({})를 사용합니다.", baseDir, FALLBACK_BASE_DIR);
            return fallbackDirectory;
        }
    }

    private String sanitize(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return "order";
        }
        return orderNo.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
