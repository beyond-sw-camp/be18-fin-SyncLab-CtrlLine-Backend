package com.beyond.synclab.ctrlline.domain.defective.service;

import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;

public interface DefectiveService {

    GetDefectiveDetailResponseDto getDefective(Long id);
}
