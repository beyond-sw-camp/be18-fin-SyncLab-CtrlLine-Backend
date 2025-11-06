package com.beyond.synclab.ctrlline.domain.factory.dto;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateFactoryRequestDto {
    private String factoryCode;
    private String factoryName;
    private String empNo;
    private boolean isActive;

    public Factories toEntity(Users users) {
        return Factories.builder()
                        .users(users)
                        .factoryCode(this.factoryCode)
                        .factoryName(this.factoryName)
                        .isActive(this.isActive)
                        .build();
    }
}
