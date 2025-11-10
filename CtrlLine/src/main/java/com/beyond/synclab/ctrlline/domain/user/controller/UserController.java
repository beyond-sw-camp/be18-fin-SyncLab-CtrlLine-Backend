package com.beyond.synclab.ctrlline.domain.user.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.user.dto.UserListResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateRequestDto;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthServiceImpl;
import com.beyond.synclab.ctrlline.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserAuthServiceImpl userAuthServiceImpl;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserSignupResponseDto>> enroll(
            @Validated @RequestBody UserSignupRequestDto request
    ) {
        UserSignupResponseDto response = userAuthServiceImpl.enroll(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BaseResponse.of(HttpStatus.CREATED.value(), response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<UserListResponseDto>>> getUserList(
        @ModelAttribute UserSearchCommand userSearchCommand,
        @PageableDefault(sort = "empNo", direction = Direction.ASC) Pageable pageable
    ) {
        Page<UserListResponseDto> users = userService.getUserList(userSearchCommand, pageable);
        return ResponseEntity.ok(BaseResponse.ok(PageResponse.from(users)));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BaseResponse<UserResponseDto>> getUserById(
        @PathVariable Long userId
    ) {
        UserResponseDto dto = userService.getUserById(userId);
        return ResponseEntity.ok(BaseResponse.ok(dto));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponseDto>> updateUser(
        @PathVariable Long userId,
        @RequestBody UserUpdateRequestDto dto
    ) {
        UserResponseDto responseDto = userService.updateUserById(dto, userId);
        return ResponseEntity.ok(BaseResponse.ok(responseDto));
    }

}
