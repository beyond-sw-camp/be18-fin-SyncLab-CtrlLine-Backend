package com.beyond.synclab.ctrlline.domain.telemetry.repository;

import com.beyond.synclab.ctrlline.domain.telemetry.entity.MesDatas;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesDataRepository extends JpaRepository<MesDatas, Long> {
    Optional<MesDatas> findFirstByFactoryIdOrderByCreatedAtDesc(Long factoryId);

    Optional<MesDatas> findTopByFactoryIdAndCreatedAtBetweenOrderByPowerConsumptionDesc(
            Long factoryId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
