package com.beyond.synclab.ctrlline.domain.process.repository;


import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessRepository extends JpaRepository<Processes, Long> {
    Optional<Processes> findByProcessCode(String processCode);
}
