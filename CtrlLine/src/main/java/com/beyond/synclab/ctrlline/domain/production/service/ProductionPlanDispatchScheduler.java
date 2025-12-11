package com.beyond.synclab.ctrlline.domain.production.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionPlanDispatchScheduler {

    private final ProductionOrderService productionOrderService;
    @Value("${production.order.dispatch.enabled:true}")
    private boolean dispatchEnabled;
    @Value("${production.order.dispatch.log-when-disabled:true}")
    private boolean logWhenDisabled;

    @Scheduled(fixedDelayString = "${production.order.dispatch.interval-ms:60000}")
    public void dispatchDuePlans() {
        if (!dispatchEnabled) {
            if (logWhenDisabled && log.isDebugEnabled()) {
                log.debug("Production order dispatch scheduler disabled. Skipping execution.");
            }
            return;
        }
        try {
            productionOrderService.dispatchDuePlans();

            // 2) RUNNING 지연 감지 + 미래 계획 밀기
            productionOrderService.detectAndApplyRunningDelays();
        } catch (Exception ex) {
            log.error("생산계획 지시 처리 중 오류가 발생했습니다.", ex);
        }
    }
}
