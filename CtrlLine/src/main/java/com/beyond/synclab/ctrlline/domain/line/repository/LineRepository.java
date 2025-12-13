package com.beyond.synclab.ctrlline.domain.line.repository;

import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LineRepository extends JpaRepository<Lines, Long> {

    @Query(value = """
            SELECT i.item_code
            FROM item_line il
            JOIN item i ON il.item_id = i.item_id
            WHERE il.line_id = :lineId
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findItemCodeByLineId(@Param("lineId") Long lineId);

    @Query(value = """
            SELECT f.factory_code
            FROM line l
            JOIN factory f ON l.factory_id = f.factory_id
            WHERE l.line_id = :lineId
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findFactoryCodeByLineId(@Param("lineId") Long lineId);

    Optional<Lines> findBylineCodeAndIsActiveTrue(String lineCode);

    Page<Lines> findAll(Specification<Lines> spec, Pageable pageable);

    Lines getReferenceByLineCode(String lineCode);

    Optional<Lines> findBylineCode(String lineCode);
}
