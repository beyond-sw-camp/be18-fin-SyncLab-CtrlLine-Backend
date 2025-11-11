package com.beyond.synclab.ctrlline.domain.telemetry.repository;

import com.beyond.synclab.ctrlline.domain.telemetry.entity.MesDatas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesDataRepository extends JpaRepository<MesDatas, Long> {
}
