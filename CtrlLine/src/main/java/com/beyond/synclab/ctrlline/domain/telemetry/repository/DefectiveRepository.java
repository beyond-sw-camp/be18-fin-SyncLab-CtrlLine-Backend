package com.beyond.synclab.ctrlline.domain.telemetry.repository;

import com.beyond.synclab.ctrlline.domain.telemetry.entity.Defectives;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefectiveRepository extends JpaRepository<Defectives, Long> {

    boolean existsByDefectiveName(String defectiveName);

    Optional<Defectives> findByDefectiveName(String defectiveName);
}
