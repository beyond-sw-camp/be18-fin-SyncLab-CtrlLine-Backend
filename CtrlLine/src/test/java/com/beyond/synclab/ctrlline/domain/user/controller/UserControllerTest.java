package com.beyond.synclab.ctrlline.domain.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.config.TestSecurityConfig;
import com.beyond.synclab.ctrlline.domain.user.dto.UserListResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserPosition;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserRole;
import com.beyond.synclab.ctrlline.domain.user.entity.Users.UserStatus;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthService;
import com.beyond.synclab.ctrlline.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class UserControllerTestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public UserAuthService userAuthService() {
            return mock(UserAuthService.class);
        }
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthService userAuthService;

    @AfterEach
    void tearDown() {
        reset(userService);
        reset(userAuthService);
    }

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

    // ======== Test Case: 유저등록 성공 ========
    @Test
    @DisplayName("유저등록 어드민 권한으로 성공 - 201 CREATED")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void signup_whenAdmin_success() throws Exception {
        // given
        String name = "홍길동";
        UserSignupRequestDto requestDto = buildSignupRequest(name);

        UserSignupResponseDto responseDto = UserSignupResponseDto.builder()
                .empNo("2025/10/21-1")
                .userName(name)
                .userEmail("hong123@test.com")
                .userRole(UserRole.ADMIN)
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
                .andExpect(jsonPath("$.data.userEmail").value("hong123@test.com"));
    }

    @Test
    @DisplayName("유저등록 권한 없음 - 403 FORBIDDEN")
    @WithMockUser(username = "user@test.com")
    void signup_forbidden_fail() throws Exception {
        // given
        String name = "홍길동";
        UserSignupRequestDto requestDto = buildSignupRequest(name);

        UserSignupResponseDto responseDto = UserSignupResponseDto.builder()
            .empNo("2025/10/21-1")
            .userName(name)
            .userEmail("hong123@test.com")
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
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    // ======== Test Case: Validation 실패 ========
    @Test
    @DisplayName("유저등록 실패 - 이름 누락 시 400 BAD_REQUEST")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
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
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN", "MANAGER", "USER"})
    void getUserList_defaultPagingAndSort() throws Exception {
        // given
        UserListResponseDto user = UserListResponseDto.builder()
            .id(1L)
            .userName("홍길동")
            .userDepartment("개발팀")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.USER)
            .userPhoneNumber("010-1234-1234")
            .empNo("2025/10/21-1")
            .build();

        Page<UserListResponseDto> mockPage = new PageImpl<>(
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
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void getUserList_noSearchParams() throws Exception {
        // given
        Page<UserListResponseDto> emptyPage = new PageImpl<>(List.of());
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

    @Test
    @DisplayName("사용자 상세 조회 성공 - 200 OK")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void getUser_success() throws Exception {
        // given
        Long userId = 1L;
        UserResponseDto userResponseDto = UserResponseDto.builder()
            .id(userId)
            .empNo("2025/10/21-1")
            .userName("홍길동")
            .userDepartment("개발팀")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.USER)
            .userPosition(UserPosition.ASSISTANT)
            .userPhoneNumber("010-1234-1234")
            .userEmail("test@test.com")
            .createdAt(LocalDateTime.now())
            .build();

        // when
        when(userService.getUserById(userId)).thenReturn(userResponseDto);

        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userName").value("홍길동"))
            .andExpect(jsonPath("$.data.userEmail").value("test@test.com"))
            .andDo(print());
    }

    @Test
    @DisplayName("상세 조회 유저 찾을 수 없음 - 404 NOT FOUND")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void getUser_notFound() throws Exception {
        // given
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new AppException(UserErrorCode.USER_NOT_FOUND));

        ResultActions resultActions = mockMvc.perform(
            get("/api/v1/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("유저 수정 성공 - 200")
    @WithMockUser(roles = {"ADMIN"})
    void patchUser_success() throws Exception {
        //given
        Long userId = 1L;

        UserResponseDto userResponseDto = UserResponseDto.builder()
            .id(userId)
            .empNo("2025/10/21-1")
            .userName("홍길동")
            .userDepartment("testDepartment")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.USER)
            .userPosition(UserPosition.ASSISTANT)
            .userPhoneNumber("010-1234-1234")
            .userEmail("hong1234@test.com")
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.updateUserById(any(UserUpdateRequestDto.class), eq(userId))).thenReturn(userResponseDto);

        // when
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userResponseDto)));

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}