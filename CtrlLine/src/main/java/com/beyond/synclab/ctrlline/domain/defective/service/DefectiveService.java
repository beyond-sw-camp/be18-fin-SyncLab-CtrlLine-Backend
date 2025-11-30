package com.beyond.synclab.ctrlline.domain.defective.service;

import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveAllResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveTypesResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveAllRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DefectiveService {

    GetDefectiveDetailResponseDto getDefective(Long id);

    Page<GetDefectiveListResponseDto> getDefectiveList(SearchDefectiveListRequestDto requestDto, Pageable pageable);

    List<GetDefectiveAllResponseDto> getAllDefective(SearchDefectiveAllRequestDto requestDto);

    GetDefectiveTypesResponseDto getDefectiveTypes(String factoryCode);
}
