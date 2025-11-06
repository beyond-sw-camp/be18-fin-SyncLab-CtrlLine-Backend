package com.beyond.synclab.ctrlline.domain.user.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthService;
import com.beyond.synclab.ctrlline.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserAuthService userAuthService;

    // ======== Fixture Builder ========
    private UserSignupRequestDto buildSignupRequest(String name) {
        return UserSignupRequestDto.builder()
                .name(name)
                .email("hong@test.com")
                .password("12341234")
                .status(Users.UserStatus.ACTIVE)
                .phoneNumber("010-1234-1234")
                .address("화산로")
                .department("영업1팀")
                .position(Users.UserPosition.ASSISTANT)
                .role(Users.UserRole.USER)
                .hiredDate(LocalDate.of(2025, 10, 20))
                .build();
    }

    // ======== Test Case: 회원가입 성공 ========
    @Test
    @DisplayName("회원가입 성공 - 201 CREATED")
    void signup_success() throws Exception {
        // given
        String name = "홍길동";
        UserSignupRequestDto requestDto = buildSignupRequest(name);

        UserSignupResponseDto responseDto = UserSignupResponseDto.builder()
                .empNo("2025/10/21-1")
                .userName(name)
                .userEmail("hong@test.com")
                .userRole(UserRole.USER)
                .build();

        when(userAuthService.enroll(any(UserSignupRequestDto.class)))
                .thenReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)));

        // then
        result
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.empNo").value("2025/10/21-1"))
                .andExpect(jsonPath("$.data.userName").value(name))
                .andExpect(jsonPath("$.data.userEmail").value("hong@test.com"));
    }

    // ======== Test Case: Validation 실패 ========
    @Test
    @DisplayName("회원가입 실패 - 이름 누락 시 400 BAD_REQUEST")
    void signup_validation_fail() throws Exception {
        // given
        UserSignupRequestDto invalidRequest = buildSignupRequest(null);

        // when & then
        ResultActions result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        result
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("사용자 목록 조회 API - 기본 페이징, 정렬")
    void getUserList_defaultPagingAndSort() throws Exception {
        // given
        UserResponseDto user = UserResponseDto.builder()
            .id(1L)
            .userName("홍길동")
            .userDepartment("개발팀")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.USER)
            .userPhoneNumber("010-1234-1234")
            .empNo("2025/10/21-1")
            .build();

        Page<UserResponseDto> mockPage = new PageImpl<>(
            List.of(user),
            PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "empNo")),
            1
        );

        when(userService.getUserList(any(UserSearchCommand.class), any(Pageable.class)))
            .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/api/v1/users")
                .param("userDepartment", "개발팀")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "empNo,asc")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].userName").value("홍길동"))
            .andExpect(jsonPath("$.data.pageInfo.currentPage").value(1))
            .andExpect(jsonPath("$.data.pageInfo.pageSize").value(10))
            .andExpect(jsonPath("$.data.pageInfo.sort[0].sortBy").value("empNo"))
            .andExpect(jsonPath("$.data.pageInfo.sort[0].direction").value("asc"))
            .andDo(print());
    }

    @Test
    @DisplayName("사용자 목록 조회 API - 검색 조건 없는 경우")
    void getUserList_noSearchParams() throws Exception {
        // given
        Page<UserResponseDto> emptyPage = new PageImpl<>(List.of());
        when(userService.getUserList(any(UserSearchCommand.class), any(Pageable.class)))
            .thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.pageInfo.totalElements").value(0))
            .andDo(print());
    }

}