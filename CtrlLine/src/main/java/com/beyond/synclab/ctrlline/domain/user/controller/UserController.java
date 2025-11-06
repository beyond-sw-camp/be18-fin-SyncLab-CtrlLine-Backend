package com.beyond.synclab.ctrlline.domain.user.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.common.dto.PageResponse;
import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthService;
import com.beyond.synclab.ctrlline.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final UserAuthService userAuthService;

    @PostMapping
    public ResponseEntity<BaseResponse<UserSignupResponseDto>> enroll(
            @Validated @RequestBody UserSignupRequestDto request
    ) {
        UserSignupResponseDto response = userAuthService.enroll(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BaseResponse.of(HttpStatus.CREATED.value(), response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<UserResponseDto>>> getUserList(
        @ModelAttribute UserSearchCommand userSearchCommand,
        @PageableDefault(sort = "empNo", direction = Direction.ASC) Pageable pageable
    ) {
        Page<UserResponseDto> users = userService.getUserList(userSearchCommand, pageable);
        return ResponseEntity.ok(BaseResponse.ok(PageResponse.from(users)));
    }
}
