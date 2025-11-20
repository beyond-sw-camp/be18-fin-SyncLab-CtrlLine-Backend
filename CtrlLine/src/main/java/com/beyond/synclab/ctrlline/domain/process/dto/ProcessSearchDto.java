package com.beyond.synclab.ctrlline.domain.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor

public class ProcessSearchDto {
        private final String processCode;
        private final String processName;
        private final String userDepartment;
        private final String userName;
        private final Boolean isActive;
}
