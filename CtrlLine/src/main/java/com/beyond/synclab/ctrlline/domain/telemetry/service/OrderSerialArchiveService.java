package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.lot.entity.Lots;
import com.beyond.synclab.ctrlline.domain.lot.service.LotGeneratorService;
import com.beyond.synclab.ctrlline.domain.productionplan.repository.ProductionPlanRepository;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans;
import com.beyond.synclab.ctrlline.domain.productionplan.entity.ProductionPlans.PlanStatus;
import com.beyond.synclab.ctrlline.domain.serial.entity.ItemSerials;
import com.beyond.synclab.ctrlline.domain.serial.repository.ItemSerialRepository;
import com.beyond.synclab.ctrlline.domain.serial.storage.SerialStorageService;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.OrderSummaryTelemetryPayload;
import com.beyond.synclab.ctrlline.domain.lot.service.LotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSerialArchiveService {

    private final SerialStorageService serialStorageService;
    private final ProductionPlanRepository productionPlanRepository;
    private final LotService lotService;
    private final ItemSerialRepository itemSerialRepository;
    private final ObjectMapper objectMapper;
    private final LotGeneratorService lotGeneratorService;

    @Transactional
    public void archive(OrderSummaryTelemetryPayload payload) {
        if (payload == null) {
            return;
        }
        Optional<ProductionPlans> planOptional = findLatestPlan(payload.orderNo());
        if (planOptional.isEmpty()) {
            log.warn("생산계획을 찾을 수 없어 시리얼 파일을 저장하지 않습니다. orderNo={}", payload.orderNo());
            return;
        }
        ProductionPlans plan = planOptional.get();
        Lots lot = resolveLot(plan);
        if (lot == null) {
            log.warn("LOT 정보를 만들 수 없어 시리얼 파일을 저장하지 않습니다. planId={}", plan.getId());
            return;
        }
        byte[] payloadBytes = resolvePayloadBytes(payload);
        if (payloadBytes == null || payloadBytes.length == 0) {
            log.warn("시리얼 데이터가 비어 있어 저장을 건너뜁니다. orderNo={}", payload.orderNo());
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

    private Lots resolveLot(ProductionPlans plan) {
        return lotService.findByProductionPlanId(plan.getId())
                .orElseGet(() -> {
                    try {
                        lotGeneratorService.createLot(plan);
                        return lotService.findByProductionPlanId(plan.getId()).orElse(null);
                    } catch (Exception ex) {
                        log.warn("LOT 생성 중 예외가 발생했습니다. planId={}", plan.getId(), ex);
                        return null;
                    }
                });
    }

    private byte[] resolvePayloadBytes(OrderSummaryTelemetryPayload payload) {
        if (StringUtils.hasText(payload.goodSerialsGzip())) {
            try {
                return Base64.getDecoder().decode(payload.goodSerialsGzip());
            } catch (IllegalArgumentException ex) {
                log.warn("시리얼 gzip 데이터를 Base64 디코딩할 수 없습니다. orderNo={}", payload.orderNo(), ex);
                return new byte[0];
            }
        }
        List<String> serials = payload.goodSerials();
        if (CollectionUtils.isEmpty(serials)) {
            return new byte[0];
        }
        try {
            String json = objectMapper.writeValueAsString(serials);
            return gzip(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException ex) {
            log.warn("시리얼 리스트를 JSON으로 직렬화하지 못했습니다. orderNo={}", payload.orderNo(), ex);
        } catch (IOException ex) {
            log.warn("시리얼 리스트를 gzip으로 압축하지 못했습니다. orderNo={}", payload.orderNo(), ex);
        }
        return new byte[0];
    }

    private byte[] gzip(byte[] input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream)) {
            gzipStream.write(input);
        }
        return outputStream.toByteArray();
    }

    private Optional<ProductionPlans> findLatestPlan(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return Optional.empty();
        }
        Optional<ProductionPlans> runningPlan =
                productionPlanRepository.findFirstByDocumentNoAndStatusOrderByIdDesc(orderNo, PlanStatus.RUNNING);
        if (runningPlan.isPresent()) {
            return runningPlan;
        }
        return productionPlanRepository.findFirstByDocumentNoOrderByIdDesc(orderNo);
    }
}
