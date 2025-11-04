package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSignupResponseDto {
    private final String empNo;
    private final String userEmail;
    private final String userName;
    private final UserRole userRole;

    public static UserSignupResponseDto fromEntity(Users user) {
        return UserSignupResponseDto.builder()
                .userEmail(user.getEmail())
                .userName(user.getName())
                .empNo(user.getEmpNo())
                .userRole(user.getRole())
                .build();
    }
}
