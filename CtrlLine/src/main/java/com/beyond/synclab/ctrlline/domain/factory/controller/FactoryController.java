package com.beyond.synclab.ctrlline.domain.factory.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.service.FactoryService;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/factories")
@RequiredArgsConstructor
public class FactoryController {

    private final FactoryService factoryService;

    @PostMapping
    public ResponseEntity<BaseResponse<FactoryResponseDto>> createFactory(
            @AuthenticationPrincipal CustomUserDetails user, @RequestBody
            CreateFactoryRequestDto request) {

        FactoryResponseDto responseDto = factoryService.createFactory(user.getUser(), request);

        BaseResponse<FactoryResponseDto> response = BaseResponse.of(HttpStatus.CREATED.value(), responseDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(response);
    }

    @PatchMapping("/{factoryCode}")
    public ResponseEntity<BaseResponse<FactoryResponseDto>> updateFactoryStatus(
            @AuthenticationPrincipal CustomUserDetails user, @RequestBody
            UpdateFactoryRequestDto request, @PathVariable("factoryCode") String factoryCode) {

        FactoryResponseDto responseDto = factoryService.updateFactoryStatus(user.getUser(), request, factoryCode);

        BaseResponse<FactoryResponseDto> response = BaseResponse.of(HttpStatus.OK.value(), responseDto);

        return ResponseEntity.ok(response);
    }
}
