package com.beyond.synclab.ctrlline.domain.lot.repository.query;

import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LotQueryRepository {

    Page<GetLotListResponseDto> searchLotList(
            SearchLotRequestDto condition,
            Pageable pageable
    );
}
