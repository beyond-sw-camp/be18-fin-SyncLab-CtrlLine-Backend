package com.beyond.synclab.ctrlline.domain.lot.service;

import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotServiceImpl implements LotService {

    private final LotRepository lotRepository;

    // Lot 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<GetLotListResponseDto> getLotList(
            SearchLotRequestDto condition,
            Pageable pageable
    ) {
        return lotRepository.searchLotList(condition, pageable);
    }
}
