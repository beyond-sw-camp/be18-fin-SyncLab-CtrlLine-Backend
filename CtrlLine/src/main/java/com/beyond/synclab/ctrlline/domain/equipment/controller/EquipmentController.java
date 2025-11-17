package com.beyond.synclab.ctrlline.domain.equipment.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.equipment.dto.CreateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentSearchResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.UpdateEquipmentRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentService;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/v1/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    // 설비 등록 API
    // 관리자만 설비를 등록할 수 있다.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<EquipmentResponseDto>> registerEquipment(
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody CreateEquipmentRequestDto requestDto) {

        EquipmentResponseDto responseDto =
                equipmentService.register(user.getUser(), requestDto);

        // 이거 BaseResponse로 받다보니까, 201이 아니라, 200으로 뜨더라고요?
        //
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(responseDto));
    }

    // 설비 상세 조회
    @GetMapping("/{equipmentCode}")
    public ResponseEntity<BaseResponse<EquipmentDetailResponseDto>> getEquipmentDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("equipmentCode") String equipmentCode) {

        EquipmentDetailResponseDto responseDto = equipmentService.getEquipmentDetail(equipmentCode);
        BaseResponse<EquipmentDetailResponseDto> response = BaseResponse.of(HttpStatus.OK.value(), responseDto);

        return ResponseEntity.ok(response);
    }

<<<<<<< HEAD
=======

>>>>>>> e16a39c9ce4734a5bb3f7902776e265d18f64ee6
    // 설비 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<EquipmentSearchResponseDto>>> getEquipmentList(
            @AuthenticationPrincipal CustomUserDetails user,
            EquipmentSearchDto searchDto,
            @PageableDefault(size = 10, sort = "equipmentCode", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        PageResponse<EquipmentSearchResponseDto> response =
                equipmentService.getEquipmentsList(user.getUser(), searchDto, pageable);

        BaseResponse<PageResponse<EquipmentSearchResponseDto>> baseResponse = BaseResponse.of(HttpStatus.OK.value(), response);
        return ResponseEntity.ok(baseResponse);
    }

    // 설비 업데이트 (사용여부, 담당자만 수정 가능. 권한은 관리자만!)
    @PatchMapping("/{equipmentCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<EquipmentResponseDto>> updateEquipment(
            // 현재 로그인 한, 사용자 정보를 가져옴.
            @AuthenticationPrincipal CustomUserDetails users,
            // {equipmentCode} 값으로 매핑해줌.
            @PathVariable String equipmentCode,
            // Body를 자바 객체로 매핑해줌.
            @RequestBody UpdateEquipmentRequestDto request

    ) {
        EquipmentResponseDto responseDto = equipmentService.updateEquipment(
                // 요청 보내는 사람의 정보 가져오기 위함.
                users.getUser(),
                // 수정 요청 데이터
                request,
                // 설비 코드로, 설비 찾음.
                equipmentCode
        );
        return ResponseEntity.ok(BaseResponse.ok(responseDto));

    }
}

