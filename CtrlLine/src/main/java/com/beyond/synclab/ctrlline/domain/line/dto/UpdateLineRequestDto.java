package com.beyond.synclab.ctrlline.domain.line.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateLineRequestDto {

    @Size(max = 100)
    private String lineName;
    private String empNo;
    private String userName;
    private Boolean isActive;
}
