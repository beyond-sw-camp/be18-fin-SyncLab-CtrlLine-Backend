package com.beyond.synclab.ctrlline.domain.item.service;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemCodeConflictException;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemNotFoundException;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    /* ================================
       🔹 단건 조회 (PK 기반)
    ================================= */
    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("ID: " + itemId));
    }

    /* ================================
       🔹 목록 조회 (Filter 기반)
    ================================= */
    @Override
    public List<Item> searchByItemCode(String code) {
        return itemRepository.findByItemCodeContaining(code);
    }

    @Override
    public List<Item> searchByItemName(String name) {
        return itemRepository.findByItemNameContaining(name);
    }

    @Override
    public List<Item> searchByItemSpecification(String spec) {
        return itemRepository.findByItemSpecificationContaining(spec);
    }

    @Override
    public List<Item> searchByStatus(ItemStatus status) {
        return itemRepository.findByItemStatus(status);
    }

    @Override
    public List<Item> searchByIsActive(boolean isActive) {
        return itemRepository.findByIsActive(isActive);
    }

    /* ================================
       🔹 신규 등록 (itemCode 중복 방지)
    ================================= */
    @Override
    @Transactional
    public Item createItem(Item item) {
        if (itemRepository.existsByItemCode(item.getItemCode())) {
            log.warn("[ITEM-CONFLICT] Duplicate itemCode detected: {}", item.getItemCode());
            throw new ItemCodeConflictException(item.getItemCode());
        }

        Item saved = itemRepository.save(item);
        log.info("[ITEM-CREATE] New item created: {}", saved.getItemCode());
        return saved;
    }

    /* ================================
       🔹 수정 (PK 기반, itemCode 포함 업데이트)
    ================================= */
    @Override
    @Transactional
    public Item updateItem(Long itemId, Item updated) {
        Item existing = getItemById(itemId);

        // itemCode 변경 시 중복 검증
        if (!existing.getItemCode().equals(updated.getItemCode())
                && itemRepository.existsByItemCode(updated.getItemCode())) {
            log.warn("[ITEM-CONFLICT] Duplicate itemCode detected during update: {}", updated.getItemCode());
            throw new ItemCodeConflictException(updated.getItemCode());
        }

        // 도메인 메서드 기반 전체 갱신
        existing.updateItem(
                updated.getItemCode(),
                updated.getItemName(),
                updated.getItemSpecification(),
                updated.getItemUnit(),
                updated.getItemStatus()
        );

        log.info("[ITEM-UPDATE] Item updated (ID: {}, Code: {})", itemId, updated.getItemCode());
        return existing;
    }

    /* ================================
       🔹 활성화 / 비활성화 (PK 기반)
    ================================= */
    @Override
    @Transactional
    public void deactivateItem(Long itemId) {
        Item item = getItemById(itemId);
        item.deactivate();
        log.info("[ITEM-DEACTIVATE] Item set inactive: {}", itemId);
    }

    @Override
    @Transactional
    public void activateItem(Long itemId) {
        Item item = getItemById(itemId);
        item.activate();
        log.info("[ITEM-ACTIVATE] Item set active: {}", itemId);
    }
}
