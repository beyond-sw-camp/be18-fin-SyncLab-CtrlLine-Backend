package com.beyond.synclab.ctrlline.domain.item.repository;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {


    /* ================================
       🔹 품목코드 기반 조회
    ================================= */
    /** 품목코드 단건 조회 (정확 일치) */
    Optional<Item> findByItemCode(String itemCode);

    /** 품목코드 부분 일치 조회 (검색용) */
    List<Item> findByItemCodeContaining(String itemCode);


    /* ================================
       🔹 품목명 기반 조회
    ================================= */
    /** 품목명 단건 조회 (정확 일치) */
    Optional<Item> findByItemName(String itemName);

    /** 품목명 부분 일치 조회 */
    List<Item> findByItemNameContaining(String itemName);


    /* ================================
       🔹 품목규격 기반 조회
    ================================= */
    /** 품목규격 단건 조회 (정확 일치) */
    Optional<Item> findByItemSpecification(String itemSpecification);

    /** 품목규격 부분 일치 조회 */
    List<Item> findByItemSpecificationContaining(String itemSpecification);


    /* ================================
       🔹 기타 조회 조건
    ================================= */
    /** 품목 상태별 조회 */
    List<Item> findByItemStatus(ItemStatus itemStatus);

    /** 품목 활성/비활성 여부별 조회 */
    List<Item> findByIsActive(boolean isActive);

    /** 품목 코드 중복 여부 확인 */
    boolean existsByItemCode(String itemCode);
}
