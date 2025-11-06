package com.beyond.synclab.ctrlline.domain.factory.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.service.FactoryService;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<BaseResponse<CreateFactoryResponseDto>> createFactory(
            @AuthenticationPrincipal CustomUserDetails user, @RequestBody CreateFactoryRequestDto request) {

        CreateFactoryResponseDto responseDto = factoryService.createFactory(user.getUser(), request);

        BaseResponse<CreateFactoryResponseDto> response = BaseResponse.of(HttpStatus.CREATED.value(), responseDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(response);
    }
}
