package com.beyond.synclab.ctrlline.domain.item.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.item.dto.request.ItemStatusUpdateRequest;
import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /* ========================================================
       🔹 품목 등록
    ======================================================== */
    @PostMapping
    public ResponseEntity<BaseResponse<Item>> createItem(@RequestBody Item request) {
        Item savedItem = itemService.createItem(request);
        log.info("[ITEM-CREATE] itemCode={} 등록 성공", savedItem.getItemCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(savedItem));
    }

    /* ========================================================
       🔹 품목 단건 조회 (itemId 기준)
    ======================================================== */
    @GetMapping("/{itemId}")
    public ResponseEntity<BaseResponse<Item>> getItemById(@PathVariable Long itemId) {
        Item item = itemService.getItemById(itemId);
        log.info("[ITEM-DETAIL] itemId={} 조회 성공", itemId);
        return ResponseEntity.ok(BaseResponse.ok(item));
    }

    /* ========================================================
       🔹 품목 목록 조회 (필터링)
    ======================================================== */
    @GetMapping
    public ResponseEntity<BaseResponse<List<Item>>> getItems(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemSpecification,
            @RequestParam(required = false) ItemStatus itemStatus,
            @RequestParam(required = false) Boolean isActive
    ) {
        log.info("[ITEM-LIST] 조회 요청 - code={}, name={}, status={}, active={}",
                itemCode, itemName, itemStatus, isActive);

        List<Item> results;

        if (itemCode != null) {
            results = itemService.searchByItemCode(itemCode);
        } else if (itemName != null) {
            results = itemService.searchByItemName(itemName);
        } else if (itemSpecification != null) {
            results = itemService.searchByItemSpecification(itemSpecification);
        } else if (itemStatus != null) {
            results = itemService.searchByStatus(itemStatus);
        } else if (isActive != null) {
            results = itemService.searchByIsActive(isActive);
        } else {
            // 필터 없이 전체 조회
            results = itemService.searchByIsActive(true);
        }

        return ResponseEntity.ok(BaseResponse.ok(results));
    }

    /* ========================================================
       🔹 품목 수정 (itemId 기준)
    ======================================================== */
    @PatchMapping("/{itemId}")
    public ResponseEntity<BaseResponse<Item>> updateItem(
            @PathVariable Long itemId,
            @RequestBody Item request
    ) {
        Item updated = itemService.updateItem(itemId, request);
        log.info("[ITEM-UPDATE] itemId={} 수정 완료", itemId);
        return ResponseEntity.ok(BaseResponse.ok(updated));
    }

    /* ========================================================
   🔹 품목 사용/미사용
======================================================== */
    @PatchMapping
    public ResponseEntity<BaseResponse<String>> updateItemStatus(
            @RequestBody ItemStatusUpdateRequest request
    ) {
        request.getItemIds().forEach(id -> {
            if (Boolean.TRUE.equals(request.getIsActive())) {
                itemService.activateItem(id);
            } else {
                itemService.deactivateItem(id);
            }
        });

        log.info("[ITEM-STATUS] {}건 상태 변경 완료 (isActive={})",
                request.getItemIds().size(), request.getIsActive());

        return ResponseEntity.ok(BaseResponse.ok("품목 사용여부가 수정되었습니다."));
    }
}
