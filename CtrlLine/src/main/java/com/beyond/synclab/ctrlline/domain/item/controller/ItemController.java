package com.beyond.synclab.ctrlline.domain.item.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.item.dto.request.CreateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemActRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemListResponseDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.UpdateItemActResponseDto;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.beyond.synclab.ctrlline.common.dto.BaseResponse.ok;

@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<BaseResponse<GetItemDetailResponseDto>> createItem(
            @RequestBody CreateItemRequestDto request) {
        var created = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(created));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Page<GetItemListResponseDto>>> getItemList(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemSpecification,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, page = 0, sort = "createdAt") Pageable pageable
    ) {
        var result = itemService.getItemList(itemCode, itemName, itemSpecification, isActive, pageable);
        return ResponseEntity.ok(ok(result));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<BaseResponse<GetItemDetailResponseDto>> getItemDetail(@PathVariable Long itemId) {
        var detail = itemService.getItemDetail(itemId);
        return ResponseEntity.ok(ok(detail));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<BaseResponse<GetItemDetailResponseDto>> updateItem(
            @PathVariable Long itemId,
            @RequestBody UpdateItemRequestDto request
    ) {
        var updated = itemService.updateItem(itemId, request);
        return ResponseEntity.ok(ok(updated));
    }

    @PatchMapping
    public ResponseEntity<BaseResponse<UpdateItemActResponseDto>> updateItemAct(
            @RequestBody UpdateItemActRequestDto request) {
        Boolean updated = itemService.updateItemAct(request);
        return ResponseEntity.ok(ok(UpdateItemActResponseDto.of(updated)));
    }
}