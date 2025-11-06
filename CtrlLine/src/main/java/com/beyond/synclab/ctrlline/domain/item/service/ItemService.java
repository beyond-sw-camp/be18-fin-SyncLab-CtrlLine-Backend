package com.beyond.synclab.ctrlline.domain.item.service;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import java.util.List;

public interface ItemService {

    // 단건 조회
    Item getItemByCode(String itemCode);
    Item getItemByName(String itemName);
    Item getItemBySpecification(String specification);

    // 목록 조회
    List<Item> searchByItemCode(String code);
    List<Item> searchByItemName(String name);
    List<Item> searchByItemSpecification(String spec);
    List<Item> searchByStatus(ItemStatus status);
    List<Item> searchByIsActive(boolean isActive);

    // 등록 / 수정 / 상태변경
    Item createItem(Item item);
    Item updateItem(String itemCode, Item updated);
    void deactivateItem(String itemCode);
    void activateItem(String itemCode);
}
