package com.beyond.synclab.ctrlline.domain.user.dto;

import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSignupRequestDto {
    @JsonProperty("userName")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @JsonProperty("userEmail")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @JsonProperty("userPassword")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, max = 20, message = "비밀번호는 6~20자 사이여야 합니다.")
    private String password;

    @JsonProperty("userStatus")
    @NotNull(message = "상태는 필수입니다.")
    private Users.UserStatus status;

    @JsonProperty("userPhoneNumber")
    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;

    @JsonProperty("userAddress")
    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @JsonProperty("userDepartment")
    @NotBlank(message = "부서는 필수입니다.")
    private String department;

    @JsonProperty("userPosition")
    @NotNull(message = "직급은 필수입니다.")
    private Users.UserPosition position;

    @JsonProperty("userRole")
    @NotNull(message = "권한은 필수입니다.")
    private Users.UserRole role;

    @JsonProperty("userHiredDate")
    @NotNull(message = "입사일은 필수입니다.")
    private LocalDate hiredDate;

    @JsonProperty("userTerminationDate")
    private LocalDate terminationDate;

    public Users toEntity(String empNo, String password) {
        return Users.builder()
                .name(this.name)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .address(this.address)
                .status(Users.UserStatus.ACTIVE)
                .password(password)
                .department(this.department)
                .position(this.position)
                .role(this.role)
                .hiredDate(this.hiredDate)
                .terminationDate(this.terminationDate)
                .empNo(empNo) // 입사번호 생성 로직
                .build();
    }
}
