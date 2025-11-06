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
       🔹 단건 조회 (정확 일치)
    ================================= */
    @Override
    public Item getItemByCode(String itemCode) {
        return itemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ItemNotFoundException(itemCode));
    }

    @Override
    public Item getItemByName(String itemName) {
        return itemRepository.findByItemName(itemName)
                .orElseThrow(() -> new ItemNotFoundException(itemName));
    }

    @Override
    public Item getItemBySpecification(String specification) {
        return itemRepository.findByItemSpecification(specification)
                .orElseThrow(() -> new ItemNotFoundException(specification));
    }

    /* ================================
       🔹 목록 조회 (부분 일치)
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
       🔹 신규 등록
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
       🔹 수정
    ================================= */
    @Override
    @Transactional
    public Item updateItem(String itemCode, Item updated) {
        Item existing = itemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ItemNotFoundException(itemCode));

        existing.updateItem(
                updated.getItemName(),
                updated.getItemSpecification(),
                updated.getItemUnit(),
                updated.getItemStatus()
        );

        log.info("[ITEM-UPDATE] Item updated: {}", itemCode);
        return existing;
    }

    /* ================================
       🔹 활성화 / 비활성화
    ================================= */
    @Override
    @Transactional
    public void deactivateItem(String itemCode) {
        Item item = itemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ItemNotFoundException(itemCode));

        item.deactivate();
        log.info("[ITEM-DEACTIVATE] Item set inactive: {}", itemCode);
    }

    @Override
    @Transactional
    public void activateItem(String itemCode) {
        Item item = itemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ItemNotFoundException(itemCode));

        item.activate();
        log.info("[ITEM-ACTIVATE] Item set active: {}", itemCode);
    }
}
