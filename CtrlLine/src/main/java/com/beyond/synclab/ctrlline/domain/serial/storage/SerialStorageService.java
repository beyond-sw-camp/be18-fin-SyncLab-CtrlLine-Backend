package com.beyond.synclab.ctrlline.domain.serial.storage;

public interface SerialStorageService {

    /**
     * Stores the provided gzip-compressed payload and returns the accessible path (local path or S3 URL).
     */
    String store(String orderNo, byte[] gzipPayload);
}
