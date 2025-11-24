package com.beyond.synclab.ctrlline.domain.defective.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.defective.dto.GetDefectiveDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.defective.service.DefectiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/defectives")
public class DefectiveController {
    private final DefectiveService defectiveService;

    @GetMapping("/{documentNo}")
    public ResponseEntity<BaseResponse<GetDefectiveDetailResponseDto>> getDefective(
        @PathVariable String documentNo
    ) {
        GetDefectiveDetailResponseDto responseDto = defectiveService.getDefective(documentNo);

        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }
}
