package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.repository.FactoryRepository;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactoryServiceImplTest {
    @InjectMocks
    private FactoryServiceImpl factoryService;

    @Mock
    private FactoryRepository factoryRepository;

    @Mock
    private UserRepository userRepository;

    private Users buildTestUser(String name, Users.UserRole userRole) {
        return Users.builder()
                     .name(name)
                     .empNo("202411001")
                     .email("hong@test.com")
                     .password("12341234")
                     .status(Users.UserStatus.ACTIVE)
                     .phoneNumber("010-1234-1234")
                     .address("화산로")
                     .department("생산1팀")
                     .position(Users.UserPosition.DIRECTOR)
                     .role(userRole)
                     .hiredDate(LocalDate.of(2025, 10, 20))
                     .build();
    }

    private Factories buildTestFactory(Users user, boolean isActive) {
        return Factories.builder()
                        .users(user)
                        .factoryCode("F001")
                        .factoryName("제1공장")
                        .isActive(isActive)
                        .build();
    }


    @Test
    @DisplayName("공장코드가 중복되면 등록에 실패한다.")
    void createFactory_fail_duplicateFactoryCode() {
        // given
        Users user = buildTestUser("홍길동",  Users.UserRole.ADMIN);
        CreateFactoryRequestDto factoryRequest = CreateFactoryRequestDto.builder()
                                                                        .factoryCode("F001")
                                                                        .factoryName("제1공장")
                                                                        .empNo(user.getEmpNo())
                                                                        .isActive(true).build();

        Factories existingFactory = buildTestFactory(user, true);

        // when
        when(factoryRepository.findByFactoryCode(factoryRequest.getFactoryCode())).thenReturn(java.util.Optional.of(existingFactory));

        // then
        assertThatThrownBy(() -> factoryService.createFactory(user, factoryRequest))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("이미 존재하는 공장코드입니다.");
    }

}
