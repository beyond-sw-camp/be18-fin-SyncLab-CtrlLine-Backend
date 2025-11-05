package com.beyond.synclab.ctrlline.domain.equipment.controller;

import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * 설비 등록 API
     * @param requestDto 요청 데이터 (equipmentCode, equipmentName 등)
     * @return 등록된 설비 정보 (201 Created)
     */

    // @Valid 검증도 없고, 예외 처리도 안 함. 그래서 무조건 201만 나옴.
    @PostMapping
    public ResponseEntity<EquipmentRegisterResponseDto> registerEquipment(
            @RequestBody EquipmentRegisterRequestDto requestDto) {

        EquipmentRegisterResponseDto responseDto = equipmentService.register(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
