package com.beyond.synclab.ctrlline.domain.defective.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveAllResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveAllRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.SearchDefectiveListRequestDto;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveListResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.service.DefectiveService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/defectives")
public class DefectiveController {
    private final DefectiveService defectiveService;

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<GetDefectiveDetailResponseDto>> getDefective(
        @PathVariable Long id
    ) {
        GetDefectiveDetailResponseDto responseDto = defectiveService.getDefective(id);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<GetDefectiveListResponseDto>>> getDefectiveList(
        @ModelAttribute SearchDefectiveListRequestDto requestDto,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
    ) {
        Page<GetDefectiveListResponseDto> responseDto = defectiveService.getDefectiveList(requestDto, pageable);

        return ResponseEntity.ok(BaseResponse.ok(PageResponse.from(responseDto)));
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<GetDefectiveAllResponseDto>>> getDefectiveAll(
        @ModelAttribute SearchDefectiveAllRequestDto requestDto
    ) {
        List<GetDefectiveAllResponseDto> responseDto = defectiveService.getAllDefective(requestDto);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }
}
