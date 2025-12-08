package com.beyond.synclab.ctrlline.domain.serial.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.common.exception.CommonErrorCode;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.exception.LotNotFoundException;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.serial.dto.response.GetLotSerialListResponseDto;
import com.beyond.synclab.ctrlline.domain.serial.entity.ItemSerials;
import com.beyond.synclab.ctrlline.domain.serial.repository.ItemSerialRepository;
import com.beyond.synclab.ctrlline.domain.serial.storage.SerialStorageService;
import com.beyond.synclab.ctrlline.domain.serial.util.SerialFileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SerialServiceImpl implements SerialService {

    private final ItemSerialRepository itemSerialRepository;
    private final LotRepository lotRepository;
    private final SerialStorageService serialStorageService;

    @Override
    @Transactional(readOnly = true)
    public GetLotSerialListResponseDto getSerialListByLotId(Long lotId) {

        Lots lot = lotRepository.findById(lotId)
                .orElseThrow(LotNotFoundException::new);

        ItemSerials serial = itemSerialRepository.findByLotId(lotId)
                .orElseThrow(() -> new AppException(CommonErrorCode.INVALID_REQUEST));

        List<String> serialList = SerialFileReader.readSerialFile(
                serial.getSerialFilePath(),
                serialStorageService
        );

        return GetLotSerialListResponseDto.of(
                lot.getLotNo(),
                serialList
        );
    }
}
