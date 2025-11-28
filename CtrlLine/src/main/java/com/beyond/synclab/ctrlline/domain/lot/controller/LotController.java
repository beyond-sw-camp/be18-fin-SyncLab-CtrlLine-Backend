package com.beyond.synclab.ctrlline.domain.lot.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.lot.dto.request.SearchLotRequestDto;
import com.beyond.synclab.ctrlline.domain.lot.dto.response.GetLotListResponseDto;
import com.beyond.synclab.ctrlline.domain.lot.service.LotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.beyond.synclab.ctrlline.common.dto.BaseResponse.ok;

@Slf4j
@RestController
@RequestMapping("/api/v1/lots")
@RequiredArgsConstructor
public class LotController {

    private final LotService lotService;

    // LOT 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<GetLotListResponseDto>>> getLotList(
            @ModelAttribute SearchLotRequestDto condition,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<GetLotListResponseDto> results =
                lotService.getLotList(condition, pageable);

        PageResponse<GetLotListResponseDto> response =
                PageResponse.from(results);

        return ResponseEntity.ok(ok(response));
    }
}
