package com.beyond.synclab.ctrlline.domain.factory.dto;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FactoryResponseDto {
    private final String factoryCode;
    private final String factoryName;
    private final String empNo;
    private final String name;
    private final String department;
    private final Boolean isActive;

    public static FactoryResponseDto fromEntity(Factories factory, Users user) {
        return FactoryResponseDto.builder()
                                 .factoryCode(factory.getFactoryCode())
                                 .factoryName(factory.getFactoryName())
                                 .empNo(user.getEmpNo())
                                 .name(user.getName())
                                 .department(user.getDepartment())
                                 .isActive(factory.getIsActive())
                                 .build();
    }
}
