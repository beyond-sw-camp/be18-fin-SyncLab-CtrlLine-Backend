package com.beyond.synclab.ctrlline.domain.factory.dto;

import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "공장코드는 필수입니다.")
    private String factoryCode;

    @NotBlank(message = "공장명은 필수입니다.")
    private String factoryName;

    @NotBlank(message = "사번은 필수입니다.")
    private String empNo;

    @JsonProperty("isActive")
    @NotBlank(message = "사용여부는 필수입니다.")
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
