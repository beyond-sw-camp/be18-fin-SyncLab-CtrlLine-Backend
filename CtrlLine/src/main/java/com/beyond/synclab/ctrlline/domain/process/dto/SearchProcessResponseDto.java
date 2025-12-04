package com.beyond.synclab.ctrlline.domain.process.dto;

import com.beyond.synclab.ctrlline.domain.process.entity.Processes;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class SearchProcessResponseDto {
    private final Long processId;
    private final String processCode;
    private final String processName;
    private final String userDepartment;
    private final String userName;
    private final String empNo;
    private final Boolean isActive;

    public static SearchProcessResponseDto fromEntity (Processes process, Users user) {
        return SearchProcessResponseDto.builder()
                .processId(process.getId())
                .processCode(process.getProcessCode())
                .processName(process.getProcessName())
                .userDepartment(user.getDepartment())
                .userName(user.getName())
                .empNo(user.getEmpNo())
                .isActive(process.isActive())
                .build();
    }
}
