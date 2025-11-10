package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserListResponseDto {
    private Long id;
    private String empNo;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;
    private String userDepartment;
    private UserRole userRole;
    private UserStatus userStatus;

    public static UserListResponseDto fromEntity(Users user) {
        return UserListResponseDto.builder()
            .id(user.getId())
            .empNo(user.getEmpNo())
            .userName(user.getName())
            .userDepartment(user.getDepartment())
            .userStatus(user.getStatus())
            .userRole(user.getRole())
            .userPhoneNumber(user.getPhoneNumber())
            .userEmail(user.getEmail())
            .build();
    }
}
