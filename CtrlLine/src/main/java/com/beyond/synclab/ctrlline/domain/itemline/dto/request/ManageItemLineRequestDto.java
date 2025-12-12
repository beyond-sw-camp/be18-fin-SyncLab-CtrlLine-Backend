package com.beyond.synclab.ctrlline.domain.itemline.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 라인별 생산 가능 품목 등록/수정 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ManageItemLineRequestDto {

    @NotNull(message = "생산 가능 품목 목록은 필수입니다.")
    private List<@NotBlank(message = "품목 코드는 비워둘 수 없습니다.") String> itemCodes;
}
