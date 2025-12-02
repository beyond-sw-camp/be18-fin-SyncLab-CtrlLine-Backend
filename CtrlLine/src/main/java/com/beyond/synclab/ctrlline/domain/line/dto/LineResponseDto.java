package com.beyond.synclab.ctrlline.domain.line.dto;

import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.line.entity.Lines;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LineResponseDto {
    private final Long lineId;
    private final String lineCode;
    private final String lineName;
    private final String empNo;
    private final String userName;
    private final String userDepartment;
    private final Boolean isActive;
    private final String factoryCode;
    private final String factoryName;

    public static LineResponseDto fromEntity(Lines line, Users user, Factories factory) {
        return LineResponseDto.builder()
                                 .lineId(line.getId())
                                 .lineCode(line.getLineCode())
                                 .lineName(line.getLineName())
                                 .empNo(user.getEmpNo())
                                 .factoryCode(factory.getFactoryCode())
                                 .factoryName(factory.getFactoryName())
                                 .userName(user.getName())
                                 .userDepartment(user.getDepartment())
                                 .isActive(line.getIsActive())
                                 .build();
    }
}
