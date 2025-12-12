package com.beyond.synclab.ctrlline.domain.itemline.service;

import com.beyond.synclab.ctrlline.domain.itemline.dto.request.ManageItemLineRequestDto;
import com.beyond.synclab.ctrlline.domain.itemline.dto.response.GetItemLineListResponseDto;

import java.util.List;

public interface ItemLineService {

    // 특정 라인에서 생산 가능한 품목 조회
    List<GetItemLineListResponseDto> getItemLineList(String lineCode);

    // 특정 라인에 생산 가능 품목 등록
    void createItemLine(String lineCode, ManageItemLineRequestDto requestDto);

    // 특정 라인의 생산 가능 품목 전체 수정
    void updateItemLine(String lineCode, ManageItemLineRequestDto requestDto);
}
