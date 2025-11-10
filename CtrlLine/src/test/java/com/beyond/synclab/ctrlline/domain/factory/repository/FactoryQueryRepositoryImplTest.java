package com.beyond.synclab.ctrlline.domain.factory.repository;

import com.beyond.synclab.ctrlline.domain.factory.dto.FactorySearchDto;
import com.beyond.synclab.ctrlline.domain.factory.entity.Factories;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import config.QuerydslTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureMockMvc(addFilters = false)
@Import(QuerydslTestConfig.class)
class FactoryQueryRepositoryImplTest {

    @Autowired
    private FactoryRepository factoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        factoryRepository.deleteAll();
        userRepository.deleteAll();

        Users user1 = userRepository.save(Users.builder()
                                               .empNo("202411001")
                                               .name("홍길동")
                                               .email("hong@test.com")
                                               .password("1234")
                                               .phoneNumber("010-1111-2222")
                                               .hiredDate(LocalDate.of(2025, 10, 21))
                                               .extension("123")
                                               .role(Users.UserRole.USER)
                                               .status(Users.UserStatus.ACTIVE)
                                               .department("생산부")
                                               .position(Users.UserPosition.MANAGER)
                                               .address("서울시 송파구")
                                               .build());

        Users user2 = userRepository.save(Users.builder()
                                               .empNo("202411002")
                                               .name("김철수")
                                               .email("kim@test.com")
                                               .password("1234")
                                               .phoneNumber("010-3333-4444")
                                               .hiredDate(LocalDate.of(2025, 10, 20))
                                               .extension("124")
                                               .role(Users.UserRole.USER)
                                               .status(Users.UserStatus.ACTIVE)
                                               .department("품질부")
                                               .position(Users.UserPosition.MANAGER)
                                               .address("부산시 해운대구")
                                               .build());


        factoryRepository.saveAll(List.of(
                Factories.builder()
                         .factoryCode("F001")
                         .factoryName("서울공장")
                         .users(user1)
                         .isActive(true)
                         .build(),

                Factories.builder()
                         .factoryCode("F002")
                         .factoryName("부산공장")
                         .users(user2)
                         .isActive(false)
                         .build(),

                Factories.builder()
                         .factoryCode("F003")
                         .factoryName("서울제2공장")
                         .users(user1)
                         .isActive(true)
                         .build()
        ));
    }

    @Test
    @DisplayName("공장코드로 검색 시 해당 공장만 반환된다")
    void searchFactoryList_success_FactoryCodeCondition() {
        // given
        FactorySearchDto searchDto = FactorySearchDto.builder()
                                                     .factoryCode("F001")
                                                     .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Factories> result = factoryRepository.searchFactoryList(searchDto, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getFactoryCode()).isEqualTo("F001");
    }

    @Test
    @DisplayName("부서와 활성여부로 검색 시 조건에 맞는 공장만 페이징되어 반환된다")
    void searchFactoryList_success_DepartmentAndIsActiveCondition() {
        // given
        FactorySearchDto searchDto = FactorySearchDto.builder()
                                                     .department("생산부")
                                                     .isActive(true)
                                                     .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Factories> result = factoryRepository.searchFactoryList(searchDto, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting("factoryCode")
                .containsExactlyInAnyOrder("F001", "F003");
    }

    @Test
    @DisplayName("fetchJoin을 사용하면 N+1 문제가 발생하지 않는다")
    void searchFactoryList_success_FetchJoin_PreventNPlusOne() {
        // given
        FactorySearchDto searchDto = FactorySearchDto.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Factories> result = factoryRepository.searchFactoryList(searchDto, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        result.getContent().forEach(factory -> {
            assertThat(factory.getUsers()).isNotNull();
            assertThat(factory.getUsers().getName()).isNotNull();
        });
    }
}
