package com.beyond.synclab.ctrlline.domain.lot.service;

import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LotService {

    Page<GetLotListResponseDto> getLotList(
            SearchLotRequestDto condition,
            final Pageable pageable
    );
}