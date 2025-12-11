package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserPosition;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

@Builder
public record SearchUserParameterDto(
    String userDepartment,
    UserStatus userStatus,
    List<UserRole> userRole,
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
