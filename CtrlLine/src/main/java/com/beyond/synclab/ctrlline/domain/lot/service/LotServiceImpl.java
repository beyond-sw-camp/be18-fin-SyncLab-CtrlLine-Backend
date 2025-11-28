package com.beyond.synclab.ctrlline.domain.lot.service;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.exception.LotNotFoundException;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.productionperformance.entity.ProductionPerformances;
import com.beyond.synclab.ctrlline.domain.productionperformance.exception.ProductionPerformanceNotFoundException;
import com.beyond.synclab.ctrlline.domain.productionperformance.repository.ProductionPerformanceRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.serial.repository.ItemSerialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .findByProductionPlanId(lot.getProductionPlan().getId())
                .orElseThrow(ProductionPerformanceNotFoundException::new);

        ProductionPlans plan = perf.getProductionPlan();
        Items item = plan.getItemLine().getItem();
        Lines line = plan.getItemLine().getLine();

        List<String> serialList = itemSerialRepository
                .findByLotId(lot.getId())
                .stream()
                .map(Serials::getSerialNo)
                .toList();

        return GetLotDetailResponseDto.of(
                lot.getId(),
                lot.getLotNo(),
                line.getFactory().getFactoryCode(),
                line.getLineCode(),
                plan.getProductionManager().getEmpNo(),
                perf.getPerformanceDocumentNo(),
                lot.getRemark(),
                item.getItemCode(),
                item.getItemName(),
                perf.getTotalQty() != null ? perf.getTotalQty().intValue() : 0,
                perf.getPerformanceQty() != null ? perf.getPerformanceQty().intValue() : 0,
                perf.getPerformanceDefectiveQty() != null ? perf.getPerformanceDefectiveQty().intValue() : 0,
                perf.getPerformanceDefectiveRate() != null ? perf.getPerformanceDefectiveRate().intValue() : 0,
                serialList,
                lot.getCreatedAt(),
                lot.getUpdatedAt(),
                lot.getIsDeleted()
        );
    }

}