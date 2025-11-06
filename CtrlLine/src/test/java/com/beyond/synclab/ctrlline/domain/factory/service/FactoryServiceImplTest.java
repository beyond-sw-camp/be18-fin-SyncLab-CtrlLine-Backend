package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryCreateRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("FactoryServiceImplTest 단위 테스트")
class FactoryServiceImplTest {
    @InjectMocks
    private FactoryServiceImpl factoryService;


    private UserSignupRequestDto buildSignupRequest(String name) {
        return UserSignupRequestDto.builder()
                                   .name(name)
                                   .email("hong@test.com")
                                   .password("12341234")
                                   .status(Users.UserStatus.ACTIVE)
                                   .phoneNumber("010-1234-1234")
                                   .address("화산로")
                                   .department("생산1팀")
                                   .position(Users.UserPosition.DIRECTOR)
                                   .role(Users.UserRole.ADMIN)
                                   .hiredDate(LocalDate.of(2025, 10, 20))
                                   .build();
    }

    @Test
    @DisplayName("공장코드가 중복되면 등록에 실패한다.")
    void createFactory_fail_duplicateFactoryCode() {
        // given
        UserSignupRequestDto userRequest = buildSignupRequest("홍길동");
        FactoryCreateRequestDto factoryRequest = FactoryCreateRequestDto.builder()
                                                                        .factoryCode("F001")
                                                                        .factoryName("제1공장")
                                                                        .department(userRequest.getDepartment())
                                                                        .name(userRequest.getName())
                                                                        .isActive(true).build();

        // when
        factoryService.createFactory(factoryRequest);

        // then
        assertThatThrownBy(() -> factoryService.createFactory(factoryRequest))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("이미 존재하는 공장 코드입니다.");
    }
}
