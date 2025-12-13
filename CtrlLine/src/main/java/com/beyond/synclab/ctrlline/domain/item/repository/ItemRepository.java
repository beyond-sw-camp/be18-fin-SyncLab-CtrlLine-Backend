package com.beyond.synclab.ctrlline.domain.item.repository;

import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.repository.query.ItemQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Items, Long>, ItemQueryRepository {

    // 품목코드 중복 여부 확인
    boolean existsByItemCode(String itemCode);

    // 품목명으로 단건 검색
    Optional<Items> findByItemName(String itemName);

    // 품목코드로 단건 검색
    Optional<Items> findByItemCodeAndIsActiveTrue(String itemCode);

    // 품목구분별 조회
    List<Items> findByItemStatus(ItemStatus itemStatus);

    // 품목 사용여부별 조회
    List<Items> findByIsActive(Boolean isActive);

    // 품목 코드 목록으로 조회
    List<Items> findByItemCodeIn(List<String> itemCodes);
}
