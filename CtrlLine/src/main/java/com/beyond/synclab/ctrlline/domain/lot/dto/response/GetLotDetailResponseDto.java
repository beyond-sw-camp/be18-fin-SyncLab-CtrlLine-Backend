package com.beyond.synclab.ctrlline.domain.lot.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetLotDetailResponseDto {

    private final Long lotId;
    private final String lotNo;
    private final String factoryCode;
    private final String lineCode;
    private final String productionManagerNo;
    private final String productionPerformanceDocNo;
    private final String remark;

    private final String itemCode;
    private final String itemName;

    private final Integer lotQty;
    private final Integer performanceQty;
    private final Integer defectiveQty;
    private final Integer defectiveRate;

    private final List<String> serialList;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Boolean isDeleted;

    public static GetLotDetailResponseDto of(
            Long lotId,
            String lotNo,
            String factoryCode,
            String lineCode,
            String productionManagerNo,
            String productionPerformanceDocNo,
            String remark,
            String itemCode,
            String itemName,
            Integer lotQty,
            Integer performanceQty,
            Integer defectiveQty,
            Integer defectiveRate,
            List<String> serialList,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean isDeleted
    ) {
        return GetLotDetailResponseDto.builder()
                .lotId(lotId)
                .lotNo(lotNo)
                .factoryCode(factoryCode)
                .lineCode(lineCode)
                .productionManagerNo(productionManagerNo)
                .productionPerformanceDocNo(productionPerformanceDocNo)
                .remark(remark)
                .itemCode(itemCode)
                .itemName(itemName)
                .lotQty(lotQty)
                .performanceQty(performanceQty)
                .defectiveQty(defectiveQty)
                .defectiveRate(defectiveRate)
                .serialList(serialList)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .isDeleted(isDeleted)
                .build();
    }
}
