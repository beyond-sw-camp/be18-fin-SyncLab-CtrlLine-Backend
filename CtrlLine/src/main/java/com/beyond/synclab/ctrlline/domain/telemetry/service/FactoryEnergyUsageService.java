package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.errorcode.FactoryErrorCode;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.telemetry.dto.FactoryEnergyUsageResponse;
import com.beyond.synclab.ctrlline.domain.telemetry.entity.MesDatas;
import com.beyond.synclab.ctrlline.domain.telemetry.errorcode.TelemetryErrorCode;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.MesDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FactoryEnergyUsageService {

    private final FactoryRepository factoryRepository;
    private final MesDataRepository mesDataRepository;

    @Transactional(readOnly = true)
    public FactoryEnergyUsageResponse getLatestEnergyUsage(String factoryCode) {
        if (!StringUtils.hasText(factoryCode)) {
            throw new AppException(FactoryErrorCode.FACTORY_NOT_FOUND);
        }
        Factories factory = factoryRepository.findByFactoryCode(factoryCode)
                .orElseThrow(() -> new AppException(FactoryErrorCode.FACTORY_NOT_FOUND));

        MesDatas latest = mesDataRepository.findFirstByFactoryIdOrderByCreatedAtDesc(factory.getId())
                .orElseThrow(() -> new AppException(TelemetryErrorCode.ENERGY_DATA_NOT_FOUND));

        return FactoryEnergyUsageResponse.builder()
                .factoryCode(factory.getFactoryCode())
                .powerConsumption(latest.getPowerConsumption())
                .recordedAt(latest.getCreatedAt())
                .build();
    }
}
