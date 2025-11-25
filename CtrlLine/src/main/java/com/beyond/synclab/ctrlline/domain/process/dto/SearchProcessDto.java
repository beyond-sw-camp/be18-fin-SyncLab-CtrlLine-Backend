package com.beyond.synclab.ctrlline.domain.process.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class SearchProcessDto {
        private final String processCode;
        private final String processName;
        private final String userDepartment;
        private final String userName;
        private final Boolean isActive;
}
