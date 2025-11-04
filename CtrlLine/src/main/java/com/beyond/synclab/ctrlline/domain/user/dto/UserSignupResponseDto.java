package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserSignupResponseDto {
    private final String email;
    private final String name;
    private final String empNo;
    private final UserRole role;

    public static UserSignupResponseDto fromEntity(Users user) {
        return UserSignupResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .empNo(user.getEmpNo())
                .role(user.getRole())
                .build();
    }
}
