package com.beyond.synclab.ctrlline.domain.serial.dto.response;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GetLotSerialListResponseDto {

    // LOT 번호
    private final String lotNo;

    // 시리얼 리스트
    private final List<String> serialList;

    public static GetLotSerialListResponseDto of(String lotNo, List<String> serials) {
        return GetLotSerialListResponseDto.builder()
                .lotNo(lotNo)
                .serialList(serials)
                .build();
    }
}
