package com.beyond.synclab.ctrlline.domain.factory.controller;

import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.service.FactoryService;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FactoryController.class)
@AutoConfigureMockMvc(addFilters = true)
class FactoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FactoryService factoryService;

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

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER 역할은 공장을 등록할 수 없다.")
    void createFactory_fail_UserRole() throws Exception {
        Users user = buildTestUser("홍길동", Users.UserRole.USER);
        CreateFactoryRequestDto factoryRequest = CreateFactoryRequestDto.builder()
                                                                        .factoryCode("F001")
                                                                        .factoryName("제1공장")
                                                                        .empNo(user.getEmpNo())
                                                                        .isActive(true)
                                                                        .build();

        mockMvc.perform(post("/api/v1/factories")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(new ObjectMapper().writeValueAsString(factoryRequest)))
               .andExpect(status().isForbidden())
               .andDo(print());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("MANAGER 역할은 공장사용여부를 변경할 수 없다.")
    void updateFactoryStatus_fail_UserRole() throws Exception {
        UpdateFactoryRequestDto request = UpdateFactoryRequestDto.builder()
                                                                 .isActive(false)
                                                                 .build();

        mockMvc.perform(patch("/api/v1/factories/F001")
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(new ObjectMapper().writeValueAsString(request)))
               .andExpect(status().isForbidden())
               .andDo(print());
    }


}
