package com.beyond.synclab.ctrlline.domain.telemetry.service;

import com.beyond.synclab.ctrlline.domain.telemetry.entity.MesDatas;
import com.beyond.synclab.ctrlline.domain.telemetry.repository.MesDataRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MesPowerConsumptionService {

    private final MesDataRepository mesDataRepository;

    @Transactional
    public void savePowerConsumption(BigDecimal powerConsumption) {
        MesDatas mesData = MesDatas.builder()
                .powerConsumption(powerConsumption)
                .build();
        mesDataRepository.save(mesData);
    }
}
