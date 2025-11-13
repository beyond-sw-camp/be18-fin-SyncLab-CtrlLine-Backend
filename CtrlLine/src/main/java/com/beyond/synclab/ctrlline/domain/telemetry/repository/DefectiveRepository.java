package com.beyond.synclab.ctrlline.domain.telemetry.repository;

import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface DefectiveRepository extends JpaRepository<Defectives, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Defectives> findTopByDocumentNoStartingWithOrderByDocumentNoDesc(String documentNoPrefix);
}
