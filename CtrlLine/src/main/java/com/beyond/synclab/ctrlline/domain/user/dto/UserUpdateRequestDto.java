package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserUpdateRequestDto {
    @JsonProperty("userName")
    private String name;

    @JsonProperty("userEmail")
    private String email;

    @JsonProperty("userPhoneNumber")
    private String phoneNumber;

    @JsonProperty("userDepartment")
    private String department;

    @JsonProperty("userPosition")
    private Users.UserPosition position;

    @JsonProperty("userRole")
    private Users.UserRole role;

    @JsonProperty("userStatus")
    private Users.UserStatus status;

    @JsonProperty("userPassword")
    private String password;

    @JsonProperty("userPasswordConfirm")
    private String passwordConfirm;

    @JsonProperty("userAddress")
    private String address;

    @JsonProperty("userTerminationDate")
    private LocalDate terminationDate;

    @JsonProperty("userExtension")
    private String extension;
}

