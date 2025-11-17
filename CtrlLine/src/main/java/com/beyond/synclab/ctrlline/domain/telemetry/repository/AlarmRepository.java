package com.beyond.synclab.ctrlline.domain.telemetry.repository;

import com.beyond.synclab.ctrlline.domain.telemetry.entity.Alarms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarms, Long> {
}
