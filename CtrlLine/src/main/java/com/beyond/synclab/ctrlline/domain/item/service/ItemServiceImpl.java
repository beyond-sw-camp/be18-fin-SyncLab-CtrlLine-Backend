package com.beyond.synclab.ctrlline.domain.item.service;

import com.beyond.synclab.ctrlline.domain.item.dto.request.CreateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.SearchItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemActRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemListResponseDto;
import com.beyond.synclab.ctrlline.domain.item.entity.Items;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemCodeConflictException;
import com.beyond.synclab.ctrlline.domain.item.exception.ItemNotFoundException;
import com.beyond.synclab.ctrlline.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    // 품목 등록
    @Override
    @Transactional
    public GetItemDetailResponseDto createItem(final CreateItemRequestDto request) {
        if (itemRepository.existsByItemCode(request.getItemCode())) {
            log.warn("[ITEM-CREATE] Duplicate itemCode detected: {}", request.getItemCode());
            throw new ItemCodeConflictException(request.getItemCode());
        }

        Items saved = itemRepository.save(request.toEntity());
        log.info("[ITEM-CREATE] itemCode={} 등록 완료", saved.getItemCode());
        return GetItemDetailResponseDto.fromEntity(saved);
    }

    // 품목 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<GetItemListResponseDto> getItemList(
            final String itemCode,
            final String itemName,
            final String itemSpecification,
            final ItemStatus itemStatus,
            final Boolean isActive,
            final Pageable pageable
    ) {
        SearchItemRequestDto condition = SearchItemRequestDto.builder()
                .itemCode(itemCode)
                .itemName(itemName)
                .itemSpecification(itemSpecification)
                .itemStatus(itemStatus)
                .isActive(isActive)
                .build();

        Page<Items> result = itemRepository.searchItems(condition, pageable);
        log.info("[ITEM-LIST] 조회 완료 - count={}, filters=[code={}, name={}, active={}]",
                result.getTotalElements(), itemCode, itemName, isActive);

        return result.map(GetItemListResponseDto::fromEntity);
    }

    // 품목 상세 조회
    @Override
    @Transactional(readOnly = true)
    public GetItemDetailResponseDto getItemDetail(final Long itemId) {
        Items item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.valueOf(itemId)));

        log.info("[ITEM-DETAIL] itemId={} 조회 완료", itemId);
        return GetItemDetailResponseDto.fromEntity(item);
    }

    // 품목 수정 (단건)
    @Override
    @Transactional
    public GetItemDetailResponseDto updateItem(final Long itemId, final UpdateItemRequestDto request) {
        Items item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.valueOf(itemId)));

        if (!item.getItemCode().equals(request.getItemCode())
                && itemRepository.existsByItemCode(request.getItemCode())) {
            log.warn("[ITEM-UPDATE] Duplicate itemCode during update: {}", request.getItemCode());
            throw new ItemCodeConflictException(request.getItemCode());
        }

        item.updateItem(request);

        log.info("[ITEM-UPDATE] itemId={} 수정 완료", itemId);
        return GetItemDetailResponseDto.fromEntity(item);
    }

    // 품목 활성/비활성 처리 (다건)
    @Override
    @Transactional
    public Boolean updateItemAct(final UpdateItemActRequestDto request) {
        if (request.getItemIds() == null || request.getItemIds().isEmpty()) {
            throw new ItemNotFoundException("No itemIds provided");
        }

        request.getItemIds().forEach(id -> {
            final Items item = itemRepository.findById(id)
                    .orElseThrow(() -> new ItemNotFoundException("Item not found with id=" + id));
            item.updateItemAct(request.getIsActive());
        });

        log.info("[ITEM-ACT] {}건 isActive 변경 완료 (isActive={})",
                request.getItemIds().size(), request.getIsActive());

        return request.getIsActive();
    }
}
