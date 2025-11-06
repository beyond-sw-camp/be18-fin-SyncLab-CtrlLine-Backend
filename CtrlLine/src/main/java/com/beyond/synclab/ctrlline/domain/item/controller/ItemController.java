package com.beyond.synclab.ctrlline.domain.item.controller;

import com.beyond.synclab.ctrlline.domain.item.entity.Item;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /** 품목 등록 **/
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item request) {
        Item savedItem = itemService.createItem(request);
        return ResponseEntity.ok(savedItem);
    }

    /** 품목 수정 **/
    @PutMapping("/{itemId}")
    public ResponseEntity<Item> updateItem(
            @PathVariable Long itemId,
            @RequestBody Item request
    ) {
        Item updatedItem = itemService.updateItem(itemId, request);
        return ResponseEntity.ok(updatedItem);
    }

    /** 품목 상세 조회 **/
    @GetMapping("/detail/{itemCode}")
    public ResponseEntity<Item> getItemDetail(@PathVariable String itemCode) {
        Item item = itemService.findItemByCode(itemCode);
        return ResponseEntity.ok(item);
    }

    /** 품목 목록 조회 (페이지/검색 조건 포함) **/
    @GetMapping("/list")
    public ResponseEntity<List<Item>> getItemList() {
        List<Item> items = itemService.findItems();
        return ResponseEntity.ok(items);
    }

    /** 품목 상태별 조회 **/
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Item>> getItemsByStatus(@PathVariable ItemStatus status) {
        List<Item> items = itemService.findItemsByStatus(status);
        return ResponseEntity.ok(items);
    }

    /** 품목 사용여부별 조회 **/
    @GetMapping("/act/{act}")
    public ResponseEntity<List<Item>> getItemsByAct(@PathVariable ItemAct act) {
        List<Item> items = itemService.findItemsByAct(act);
        return ResponseEntity.ok(items);
    }
}
