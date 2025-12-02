package com.beyond.synclab.ctrlline.domain.log.repository;

import com.beyond.synclab.ctrlline.domain.log.entity.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LogRepository extends JpaRepository<Logs, Long>, JpaSpecificationExecutor<Logs> {
}
