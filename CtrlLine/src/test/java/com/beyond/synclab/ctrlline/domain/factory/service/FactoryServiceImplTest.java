package com.beyond.synclab.ctrlline.domain.factory.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.FactoryResponseDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.factory.errorcode.FactoryErrorCode;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("공장 상세 조회에 성공한다.")
    void getFactory_success() {
        // given
        Users manager = buildTestUser("홍길동", Users.UserRole.MANAGER);
        Factories factory = buildTestFactory(manager, true);
        when(factoryRepository.findByFactoryCode(factory.getFactoryCode()))
                .thenReturn(java.util.Optional.of(factory));

        // when
        FactoryResponseDto response = factoryService.getFactory(factory.getFactoryCode());

        // then
        assertThat(response.getFactoryCode()).isEqualTo(factory.getFactoryCode());
        assertThat(response.getFactoryName()).isEqualTo(factory.getFactoryName());
        assertThat(response.getName()).isEqualTo(manager.getName());
    }

    @Test
    @DisplayName("존재하지 않는 공장은 조회 시 예외가 발생한다.")
    void getFactory_notFound() {
        // given
        String factoryCode = "F999";
        when(factoryRepository.findByFactoryCode(factoryCode))
                .thenReturn(java.util.Optional.empty());

        // then
        assertThatThrownBy(() -> factoryService.getFactory(factoryCode))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(FactoryErrorCode.FACTORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("공장 사용 여부를 성공적으로 변경한다.")
    void updateFactoryStatus_success() {
        // given
        Users admin = buildTestUser("관리자", Users.UserRole.ADMIN);
        Factories factory = buildTestFactory(admin, true);
        UpdateFactoryRequestDto request = UpdateFactoryRequestDto.builder()
                .isActive(false)
                .build();

        when(factoryRepository.findByFactoryCode(factory.getFactoryCode()))
                .thenReturn(java.util.Optional.of(factory));

        // when
        FactoryResponseDto response = factoryService.updateFactoryStatus(admin, request, factory.getFactoryCode());

        // then
        assertThat(response.getIsActive()).isFalse();
        assertThat(factory.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 공장은 사용 여부 변경 시 예외가 발생한다.")
    void updateFactoryStatus_notFound() {
        // given
        Users admin = buildTestUser("관리자", Users.UserRole.ADMIN);
        UpdateFactoryRequestDto request = UpdateFactoryRequestDto.builder()
                .isActive(false)
                .build();

        when(factoryRepository.findByFactoryCode("F999"))
                .thenReturn(java.util.Optional.empty());

        // then
        assertThatThrownBy(() -> factoryService.updateFactoryStatus(admin, request, "F999"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(FactoryErrorCode.FACTORY_NOT_FOUND.getMessage());
    }

}
