package com.beyond.synclab.ctrlline.domain.item.service;

import com.beyond.synclab.ctrlline.domain.item.dto.request.CreateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemActRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.request.UpdateItemRequestDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.item.dto.response.GetItemListResponseDto;
import com.beyond.synclab.ctrlline.domain.item.entity.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {

    // 품목 등록
    GetItemDetailResponseDto createItem(final CreateItemRequestDto request);

    // 품목 목록 조회 (검색 / 페이징)
    Page<GetItemListResponseDto> getItemList(
            final String itemCode,
            final String itemName,
            final String itemSpecification,
            final ItemStatus itemStatus,
            final Boolean isActive,
            final Pageable pageable
    );

    // 품목 상세 조회
    GetItemDetailResponseDto getItemDetail(final Long itemId);

    // 품목 수정 (단건)
    GetItemDetailResponseDto updateItem(final Long itemId, final UpdateItemRequestDto request);

    // 품목 활성/비활성 처리 (다건)
    Boolean updateItemAct(final UpdateItemActRequestDto request);
}
