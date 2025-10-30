package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserSignupResponseDto {
    private final Long id;
    private final String email;
    private final String name;

    public static UserSignupResponseDto fromEntity(Users user) {
        return UserSignupResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
