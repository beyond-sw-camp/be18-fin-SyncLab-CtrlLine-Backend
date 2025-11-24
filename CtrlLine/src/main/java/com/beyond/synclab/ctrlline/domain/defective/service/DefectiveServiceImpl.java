package com.beyond.synclab.ctrlline.domain.defective.service;

import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.itemline.repository.ItemLineRepository;
import com.beyond.synclab.ctrlline.domain.line.repository.LineRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.DefectiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefectiveServiceImpl implements DefectiveService {

    private final DefectiveRepository defectiveRepository;
    private final LineRepository lineRepository;
    private final ItemLineRepository itemLineRepository;


    @Override
    @Transactional(readOnly = true)
    public GetDefectiveDetailResponseDto getDefective(String documentNo) {
        return null;
    }
}
