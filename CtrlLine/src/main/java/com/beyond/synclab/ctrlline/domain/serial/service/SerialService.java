package com.beyond.synclab.ctrlline.domain.serial.service;

import com.beyond.synclab.ctrlline.domain.serial.dto.response.GetLotSerialListResponseDto;

public interface SerialService {

    GetLotSerialListResponseDto getSerialListByLotId(Long lotId);
}
