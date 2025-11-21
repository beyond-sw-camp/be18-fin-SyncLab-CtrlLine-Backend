package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserPosition;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record SearchUserParameterDto(
    String userDepartment,
    UserStatus userStatus,
    UserRole userRole,
    UserPosition userPosition,
    String userPhoneNumber,
    String userEmail,
    String userEmpNo,
    String userName,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate hiredDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate terminationDate
) {

}
