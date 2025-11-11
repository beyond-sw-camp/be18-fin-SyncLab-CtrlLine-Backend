package com.beyond.synclab.ctrlline.domain.equipment.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterRequestDto;
import com.beyond.synclab.ctrlline.domain.equipment.dto.EquipmentRegisterResponseDto;
import com.beyond.synclab.ctrlline.domain.equipment.service.EquipmentService;
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(responseDto));
    }
}

//    // <Object>로 수정해야 .build 오류 안 뜨네여..
//    public ResponseEntity<Object> registerEquipment(
//            @Valid @RequestBody EquipmentRegisterRequestDto requestDto) {
//        try {
//            EquipmentRegisterResponseDto responseDto = equipmentService.register(requestDto);
//            // 성공적인 등록: EquipmentRegisterResponseDto 반환
//            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
//        } catch (IllegalArgumentException e) {
//            // 중복 설비 코드 처리
//            if (e.getMessage().contains("이미 존재하는 설비 코드입니다.")) {
//                return ResponseEntity.status(HttpStatus.CONFLICT)
//                        .body(new ErrorResponse(409, "EQUIPMENT_CONFLICT", e.getMessage()));
//            }
//            // 존재하지 않는 사번 예외처리
//            if (e.getMessage().contains("존재하지 않는 사번입니다.")) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(new ErrorResponse(400, "USER_NOT_FOUND", e.getMessage()));
//            }
//            // 기타 예외 처리(설비 코드 누락 경우)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ErrorResponse(400, "BAD_REQUEST", e.getMessage()));
//        } catch (AccessDeniedException e) {
//            // 인증 오류 처리 (401 Unauthorized)
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new ErrorResponse(401, "AUTH_INVALID_TOKEN", "인증 토큰이 없거나 유효하지 않습니다."));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ErrorResponse(500, "UNEXPECTED_ERROR", "예상치 못한 에러"));
//        }
//    }

    // 설비 목록 조회
//    @GetMapping
//    public ResponseEntity<BaseResponse<EquipmentListResponseDto>> getEquipments(
//        @ModelAttribute EquipmentSearchCommand equipmentSearchCommand,
//        @PageableDefault(sort="EquipmentCode", direction = Sort.Direction.ASC) Pageable pageable
//    ) {
//        Page<EquipmentListResponseDto> equipments =  equipmentService.getEquipments(equipmentSearchCommand, pageable);
//        return ResponseEntity.ok(BaseResponse.from(equipments));
//    }
//

//// 오류 응답 객체
//        public static class ErrorResponse {
//            private final int status;
//            private final String code;
//            private final String message;
//
//            public ErrorResponse(int status, String code, String message) {
//                this.status = status;
//                this.code = code;
//                this.message = message;
//            }
//
//             // Getter 메서드들
//             public int getStatus() {
//                 return status;
//             }
//
//             public String getCode() {
//                 return code;
//             }
//
//             public String getMessage() {
//                 return message;
//             }
//         }
//    }
