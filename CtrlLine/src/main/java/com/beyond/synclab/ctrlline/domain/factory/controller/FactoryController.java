package com.beyond.synclab.ctrlline.domain.factory.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactorySearchDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.service.FactoryService;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BaseResponse<FactoryResponseDto>> createFactory(
            @AuthenticationPrincipal CustomUserDetails user, @RequestBody
            CreateFactoryRequestDto request) {

        FactoryResponseDto responseDto = factoryService.createFactory(user.getUser(), request);

        BaseResponse<FactoryResponseDto> response = BaseResponse.of(HttpStatus.CREATED.value(), responseDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(response);
    }

    @GetMapping("/{factoryCode}")
    public ResponseEntity<BaseResponse<FactoryResponseDto>> getFactory(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("factoryCode") String factoryCode) {

        FactoryResponseDto responseDto = factoryService.getFactory(factoryCode);

        BaseResponse<FactoryResponseDto> response = BaseResponse.of(HttpStatus.OK.value(), responseDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<FactoryResponseDto>>> getFactoryList(
            @AuthenticationPrincipal CustomUserDetails user,
            FactorySearchDto searchDto,
            @PageableDefault(size = 10, sort = "factoryCode", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        PageResponse<FactoryResponseDto> response = factoryService.getFactoryList(user.getUser(), searchDto, pageable);

        BaseResponse<PageResponse<FactoryResponseDto>> baseResponse =
                BaseResponse.of(HttpStatus.OK.value(), response);

        return ResponseEntity.ok(baseResponse);
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
