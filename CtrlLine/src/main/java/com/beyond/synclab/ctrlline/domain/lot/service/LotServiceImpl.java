package com.beyond.synclab.ctrlline.domain.lot.service;

import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.exception.LotNotFoundException;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.exception.ProductionPerformanceNotFoundException;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.serial.entity.ItemSerials;
import com.beyond.synclab.ctrlline.domain.serial.repository.ItemSerialRepository;
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
    private final ItemSerialRepository itemSerialRepository;
    private final ProductionPerformanceRepository performanceRepository;

    // Lot 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<GetLotListResponseDto> getLotList(
            SearchLotRequestDto condition,
            Pageable pageable
    ) {
        return lotRepository.searchLotList(condition, pageable);
    }

    // Lot 상세 조회
    @Override
    @Transactional(readOnly = true)
    public GetLotDetailResponseDto getLotDetail(Long lotId) {

        Lots lot = lotRepository.findById(lotId)
                .orElseThrow(LotNotFoundException::new);

        ProductionPerformances perf = performanceRepository
                .findByProductionPlanIdAndIsDeletedFalse(lot.getProductionPlan().getId())
                .orElseThrow(ProductionPerformanceNotFoundException::new);

        String serialFilePath = itemSerialRepository.findByLotId(lot.getId())
                .map(ItemSerials::getSerialFilePath)
                .orElse(null);

        return GetLotDetailResponseDto.fromEntity(lot, perf, serialFilePath);
    }
}