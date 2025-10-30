package com.beyond.synclab.ctrlline.domain.user.controller;

import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signup(
            @Validated @RequestBody UserSignupRequestDto request
    ) {
        UserSignupResponseDto response = userAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
