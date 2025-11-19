package com.beyond.synclab.ctrlline.domain.production.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionPlanDispatchScheduler {

    private final ProductionOrderService productionOrderService;

    @Scheduled(fixedDelayString = "${production.order.dispatch.interval-ms:60000}")
    public void dispatchDuePlans() {
        try {
            productionOrderService.dispatchDuePlans();
        } catch (Exception ex) {
            log.error("생산계획 지시 처리 중 오류가 발생했습니다.", ex);
        }
    }
}
