package com.beyond.synclab.ctrlline.domain.itemline.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.itemline.dto.request.UpdateItemLineRequestDto;
import com.beyond.synclab.ctrlline.domain.itemline.dto.response.GetItemLineListResponseDto;
import com.beyond.synclab.ctrlline.domain.itemline.service.ItemLineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.beyond.synclab.ctrlline.common.dto.BaseResponse.ok;

/**
 * ItemLineController
 *
 * 라인별 생산 가능 품목 조회 및 수정 API 컨트롤러
 * CTRLLINE 컨벤션 및 ItemController 구조 준수 버전
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/item-lines")
public class ItemLineController {

    private final ItemLineService itemLineService;

     /**
     🔹 생산 가능 품목 목록 조회
     */
    @GetMapping("/{lineId}")
    public ResponseEntity<BaseResponse<List<GetItemLineListResponseDto>>> getItemLineList(
            @PathVariable final Long lineId
    ) {
        log.info("API 호출 - 라인({}) 생산 가능 품목 목록 조회 요청", lineId);

        List<GetItemLineListResponseDto> result = itemLineService.getItemLineList(lineId);
        return ResponseEntity.ok(ok(result));
    }

     /**
     🔹 생산 가능 품목 전체 수정
     */
    @PutMapping("/{lineId}")
    public ResponseEntity<BaseResponse<Void>> updateItemLine(
            @PathVariable final Long lineId,
            @RequestBody final UpdateItemLineRequestDto requestDto
    ) {
        log.info("API 호출 - 라인({}) 생산 가능 품목 수정 요청", lineId);

        itemLineService.updateItemLine(lineId, requestDto);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
