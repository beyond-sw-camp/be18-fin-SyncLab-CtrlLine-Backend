package com.beyond.synclab.ctrlline.domain.defective.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetDefectiveTypesResponseDto {
    private String factoryCode;
    private List<DefectiveTypesItems> types;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DefectiveTypesItems {
        private String defectiveName;
        private String defectiveCode;
        private String defectiveType;
        private BigDecimal defectiveCount;
    }
}
