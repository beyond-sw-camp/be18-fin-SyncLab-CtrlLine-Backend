package com.beyond.synclab.ctrlline.domain.factory.repository;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoryRepository extends JpaRepository<Factories, Long> {
}
