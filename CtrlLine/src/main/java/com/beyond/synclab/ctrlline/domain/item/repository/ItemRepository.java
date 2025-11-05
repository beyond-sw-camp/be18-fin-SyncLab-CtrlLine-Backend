package com.beyond.synclab.ctrlline.domain.item.repository;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemAct;
import com.beyond.synclab.ctrlline.domain.item.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByItemCode(String itemCode);

    List<Item> findByItemStatus(ItemStatus itemStatus);

    List<Item> findByItemAct(ItemAct itemAct);

    boolean existsByItemCode(String itemCode);
}
