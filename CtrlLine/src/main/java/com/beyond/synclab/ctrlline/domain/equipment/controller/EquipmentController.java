package com.beyond.synclab.ctrlline.domain.equipment.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentDetailResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentService;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.service.CustomUserDetails;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
   // private final JwtUtil jwtUtil;

    // 설비 등록 API
    // 관리자만 설비를 등록할 수 있다.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<EquipmentRegisterResponseDto>> registerEquipment(
            @AuthenticationPrincipal CustomUserDetails user,
            @Validated @RequestBody EquipmentRegisterRequestDto requestDto) {

        EquipmentRegisterResponseDto responseDto =
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
            @PathVariable ("equipmentCode") String equipmentCode) {

        EquipmentDetailResponseDto responseDto = equipmentService.getEquipmentDetail(equipmentCode);
        BaseResponse<EquipmentDetailResponseDto> response = BaseResponse.of(HttpStatus.OK.value(), responseDto);

        return ResponseEntity.ok(response);
    }


}

