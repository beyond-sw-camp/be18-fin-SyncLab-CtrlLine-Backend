package com.beyond.synclab.ctrlline.domain.factory.repository;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FactoryRepository extends JpaRepository<Factories, Long>, FactoryQueryRepository {
    Optional<Factories> findByFactoryCodeAndIsActiveTrue(String factoryCode);
}
