package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.repository.LotRepository;
import com.beyond.synclab.ctrlline.domain.production.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.serial.entity.ItemSerials;
import com.beyond.synclab.ctrlline.domain.serial.repository.ItemSerialRepository;
import com.beyond.synclab.ctrlline.domain.serial.storage.SerialStorageService;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSerialArchiveService {

    private final SerialStorageService serialStorageService;
    private final ProductionPlanRepository productionPlanRepository;
    private final LotRepository lotRepository;
    private final ItemSerialRepository itemSerialRepository;

    @Transactional
    public void archive(OrderSummaryTelemetryPayload payload) {
        if (payload == null || !StringUtils.hasText(payload.goodSerialsGzip())) {
            return;
        }
        Optional<ProductionPlans> planOptional = productionPlanRepository.findByDocumentNo(payload.orderNo());
        if (planOptional.isEmpty()) {
            log.warn("생산계획을 찾을 수 없어 시리얼 파일을 저장하지 않습니다. orderNo={}", payload.orderNo());
            return;
        }
        ProductionPlans plan = planOptional.get();
        Optional<Lots> lotOptional = lotRepository.findByProductionPlanId(plan.getId());
        if (lotOptional.isEmpty()) {
            log.warn("생산계획에 연결된 LOT 정보를 찾을 수 없어 시리얼 파일을 저장하지 않습니다. planId={}", plan.getId());
            return;
        }
        Lots lot = lotOptional.get();
        byte[] payloadBytes;
        try {
            payloadBytes = Base64.getDecoder().decode(payload.goodSerialsGzip());
        } catch (IllegalArgumentException ex) {
            log.warn("시리얼 gzip 데이터를 Base64 디코딩할 수 없습니다. orderNo={}", payload.orderNo(), ex);
            return;
        }
        String storedPath = serialStorageService.store(payload.orderNo(), payloadBytes);
        ItemSerials serialEntity = itemSerialRepository.findByLotId(lot.getId())
                .map(existing -> {
                    existing.updateSerialFilePath(storedPath);
                    return existing;
                })
                .orElseGet(() -> ItemSerials.create(lot.getId(), storedPath));
        itemSerialRepository.save(serialEntity);
        log.info("시리얼 gzip 파일 저장 완료 lotId={} path={}", lot.getId(), storedPath);
    }
}
