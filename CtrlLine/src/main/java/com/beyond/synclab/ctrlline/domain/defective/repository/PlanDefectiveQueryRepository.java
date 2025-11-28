package com.beyond.synclab.ctrlline.domain.defective.repository;

import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlanDefectiveQueryRepository {
        Page<GetDefectiveListResponseDto> findDefectiveList(SearchDefectiveListRequestDto request, Pageable pageable);
}
