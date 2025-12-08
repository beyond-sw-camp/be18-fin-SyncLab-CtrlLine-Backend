package com.beyond.synclab.ctrlline.domain.serial.storage;

import java.util.List;

public interface SerialStorageService {

    String store(String orderNo, byte[] gzipPayload);

    List<String> read(String path);
}
