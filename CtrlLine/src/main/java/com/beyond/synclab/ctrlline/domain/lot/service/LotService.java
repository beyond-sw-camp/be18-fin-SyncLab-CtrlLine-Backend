package com.beyond.synclab.ctrlline.domain.lot.service;

import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import com.beyond.synclab.ctrlline.domain.productionperformance.dto.response.GetProductionPerformanceDetailResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LotService {

    // Lot 목록 조회
    Page<GetLotListResponseDto> getLotList(
            SearchLotRequestDto condition,
            final Pageable pageable
    );

    // Lot 상세 조회
    GetLotDetailResponseDto getLotDetail(
            Long id
    );
}