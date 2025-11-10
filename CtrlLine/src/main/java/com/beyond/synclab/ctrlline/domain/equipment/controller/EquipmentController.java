package com.beyond.synclab.ctrlline.domain.equipment.controller;

import com.beyond.synclab.ctrlline.common.exception.ErrorResponse;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentListResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import com.beyond.synclab.ctrlline.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final JwtUtil jwtUtil;

     // 설비 등록 API

    // @Valid 검증도 없고, 예외 처리도 안 함. 그래서 무조건 201만 나옴.
    @PostMapping
    @PreAuthorize("hashRole('ADMIN')")
    // <Object>로 수정해야 .build 오류 안 뜨네여..
    public ResponseEntity<Object> registerEquipment(
            @Valid @RequestBody EquipmentRegisterRequestDto requestDto) {

        try {
            EquipmentRegisterResponseDto responseDto = equipmentService.register(requestDto);
            // 성공적인 등록: EquipmentRegisterResponseDto 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        }

        catch (IllegalArgumentException e) {
            // 중복 설비 코드 처리
            if (e.getMessage().contains("이미 존재하는 설비 코드입니다.")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(409, "EQUIPMENT_CONFLICT", e.getMessage()));
            }
            // 존재하지 않는 사번 예외처리
            if (e.getMessage().contains("존재하지 않는 사번입니다.")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(400, "USER_NOT_FOUND", e.getMessage()));
            }
            // 기타 예외 처리(설비 코드 누락 경우)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "BAD_REQUEST", e.getMessage()));
        }
        catch (AccessDeniedException e) {
            // 인증 오류 처리 (401 Unauthorized)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "AUTH_INVALID_TOKEN", "인증 토큰이 없거나 유효하지 않습니다."));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "UNEXPECTED_ERROR", "예상치 못한 에러"));
        }
    }

    // 설비 목록 조회 API
    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<EquipmentListResponseDto>>> getEquipments(
            @PageableDefault(sort = "equipmentCode", direction = Direction.ASC) Pageable pageable) {

        // Pageable 객체는 클라이언트에서 받은 페이지 번호와 페이지 크기를 자동으로 처리
        // 페이지 처리된 설비 목록을 반환
        Page<EquipmentListResponseDto> equipmentPage = equipmentService.getEquipments(pageable);

        // PageResponse로 감싸고 BaseResponse로 감싸서 반환
        PageResponse<EquipmentListResponseDto> pageResponse = PageResponse.from(equipmentPage);
        return ResponseEntity.ok(BaseResponse.ok(pageResponse));
    }

    // 오류 응답 객체
        public static class ErrorResponse {
            private final int status;
            private final String code;
            private final String message;

            public ErrorResponse(int status, String code, String message) {
                this.status = status;
                this.code = code;
                this.message = message;
            }

             // Getter 메서드들
             public int getStatus() {
                 return status;
             }

             public String getCode() {
                 return code;
             }

             public String getMessage() {
                 return message;
             }
         }
    }