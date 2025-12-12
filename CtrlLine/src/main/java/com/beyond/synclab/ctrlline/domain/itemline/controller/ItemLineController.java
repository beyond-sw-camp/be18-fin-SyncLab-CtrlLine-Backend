package com.beyond.synclab.ctrlline.domain.itemline.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.itemline.dto.request.ManageItemLineRequestDto;
import com.beyond.synclab.ctrlline.domain.itemline.dto.response.GetItemLineListResponseDto;
import com.beyond.synclab.ctrlline.domain.itemline.service.ItemLineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 생산 가능 품목 목록 조회
    @GetMapping("/{lineCode}")
    public ResponseEntity<BaseResponse<List<GetItemLineListResponseDto>>> getItemLineList(
            @PathVariable final String lineCode
    ) {
        log.info("API 호출 - 라인({}) 생산 가능 품목 목록 조회 요청", lineCode);

        List<GetItemLineListResponseDto> result = itemLineService.getItemLineList(lineCode);
        return ResponseEntity.ok(ok(result));
    }

    // 생산 가능 품목 등록
    @PostMapping("/{lineCode}")
    public ResponseEntity<BaseResponse<Void>> createItemLine(
            @PathVariable final String lineCode,
            @Valid @RequestBody final ManageItemLineRequestDto requestDto
    ) {
        log.info("API 호출 - 라인({}) 생산 가능 품목 등록 요청", lineCode);

        itemLineService.createItemLine(lineCode, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.of(HttpStatus.CREATED.value(), null));
    }

    // 생산 가능 품목 전체 수정
    @PutMapping("/{lineCode}")
    public ResponseEntity<BaseResponse<Void>> updateItemLine(
            @PathVariable final String lineCode,
            @Valid @RequestBody final ManageItemLineRequestDto requestDto
    ) {
        log.info("API 호출 - 라인({}) 생산 가능 품목 수정 요청", lineCode);

        itemLineService.updateItemLine(lineCode, requestDto);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
