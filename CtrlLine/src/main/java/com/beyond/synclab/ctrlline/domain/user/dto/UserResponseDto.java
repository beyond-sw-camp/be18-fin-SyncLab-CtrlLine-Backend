package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserPosition;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {
    private Long id;
    private String empNo;
    private String userName;
    private String userDepartment;
    private UserStatus userStatus;
    private UserRole userRole;
    private UserPosition userPosition;
    private String userPhoneNumber;
    private String userEmail;
    private String userAddress;
    private String userExtension;
    private LocalDate hiredDate;
    private LocalDate terminationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponseDto fromEntity(Users user) {
        return UserResponseDto.builder()
            .id(user.getId())
            .empNo(user.getEmpNo())
            .userName(user.getName())
            .userDepartment(user.getDepartment())
            .userStatus(user.getStatus())
            .userRole(user.getRole())
            .userPosition(user.getPosition())
            .userPhoneNumber(user.getPhoneNumber())
            .userEmail(user.getEmail())
            .userAddress(user.getAddress())
            .userExtension(user.getExtension())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .hiredDate(user.getHiredDate())
            .terminationDate(user.getTerminationDate())
            .build();
    }
}
