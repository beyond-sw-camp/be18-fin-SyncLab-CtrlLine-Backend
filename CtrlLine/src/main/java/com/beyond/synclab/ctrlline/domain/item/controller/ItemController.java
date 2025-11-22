package com.beyond.synclab.ctrlline.domain.item.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.item.dto.request.CreateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemActRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemListResponseDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.UpdateItemActResponseDto;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import com.beyond.synclab.ctrlline.domain.item.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.beyond.synclab.ctrlline.common.dto.BaseResponse.ok;

@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 품목 등록
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BaseResponse<GetItemDetailResponseDto>> createItem(
            @Valid @RequestBody CreateItemRequestDto request) {

        GetItemDetailResponseDto createdItem = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok(createdItem));
    }

    // 품목 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<GetItemListResponseDto>>> getItemList(
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String itemSpecification,
            @RequestParam(required = false) ItemStatus itemStatus,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, page = 0, sort = "itemCode") Pageable pageable
    ) {
        Page<GetItemListResponseDto> result = itemService.getItemList(
                itemCode,
                itemName,
                itemSpecification,
                itemStatus,
                isActive,
                pageable
        );

        PageResponse<GetItemListResponseDto> response = PageResponse.<GetItemListResponseDto>from(result);

        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    // 품목 상세 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<BaseResponse<GetItemDetailResponseDto>> getItemDetail(@PathVariable Long itemId) {
        GetItemDetailResponseDto detail = itemService.getItemDetail(itemId);
        return ResponseEntity.ok(ok(detail));
    }

    // 품목 수정
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{itemId}")
    public ResponseEntity<BaseResponse<GetItemDetailResponseDto>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequestDto request
    ) {
        GetItemDetailResponseDto updatedItem = itemService.updateItem(itemId, request);
        return ResponseEntity.ok(ok(updatedItem));
    }

    // 품목 사용여부 수정
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public ResponseEntity<BaseResponse<UpdateItemActResponseDto>> updateItemAct(
            @Valid @RequestBody UpdateItemActRequestDto request) {

        Boolean updated = itemService.updateItemAct(request);
        UpdateItemActResponseDto response = UpdateItemActResponseDto.of(updated);
        return ResponseEntity.ok(ok(response));
    }
}