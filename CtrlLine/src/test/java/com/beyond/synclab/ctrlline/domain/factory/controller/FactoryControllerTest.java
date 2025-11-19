package com.beyond.synclab.ctrlline.domain.factory.controller;

import com.beyond.synclab.ctrlline.domain.factory.dto.CreateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.dto.UpdateFactoryRequestDto;
import com.beyond.synclab.ctrlline.domain.factory.service.FactoryService;
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

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER 역할은 공장을 등록할 수 없다.")
    void createFactory_fail_UserRole() throws Exception {
        CreateFactoryRequestDto factoryRequest = CreateFactoryRequestDto.builder()
                                                                        .factoryCode("F001")
                                                                        .factoryName("제1공장")
                                                                        .empNo("202411001")
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
