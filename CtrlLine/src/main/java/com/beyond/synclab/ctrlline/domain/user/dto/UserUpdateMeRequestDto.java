package com.beyond.synclab.ctrlline.domain.user.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUpdateMeRequestDto {
    private String userName;
    private String userEmail;
    private String userPassword;
    private String userPasswordConfirm;
    private String userPhoneNumber;
    private String userAddress;
}
