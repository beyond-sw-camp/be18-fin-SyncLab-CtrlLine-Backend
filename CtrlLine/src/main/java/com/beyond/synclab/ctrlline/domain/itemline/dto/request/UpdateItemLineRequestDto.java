package com.beyond.synclab.ctrlline.domain.itemline.dto.request;

import lombok.*;

import java.util.List;

/**
 * 수정 탭 - 라인별 생산 가능 품목 전체 수정 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UpdateItemLineRequestDto {

    private List<String> itemCodes;
}
