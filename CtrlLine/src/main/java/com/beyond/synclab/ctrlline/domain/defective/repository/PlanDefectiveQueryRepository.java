package com.beyond.synclab.ctrlline.domain.defective.repository;

import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveAllResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveTypesResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveAllRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlanDefectiveQueryRepository {
        Page<GetDefectiveListResponseDto> findDefectiveList(SearchDefectiveListRequestDto request, Pageable pageable);

        List<GetDefectiveAllResponseDto> findAllDefective(SearchDefectiveAllRequestDto requestDto);

        GetDefectiveTypesResponseDto findDefectiveTypes(String factoryCode);
}
