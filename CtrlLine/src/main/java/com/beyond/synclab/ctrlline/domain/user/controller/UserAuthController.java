package com.beyond.synclab.ctrlline.domain.user.controller;

import com.beyond.synclab.ctrlline.common.dto.BaseResponse;
import com.beyond.synclab.ctrlline.domain.user.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {
    private final UserAuthService userAuthService;

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Map<String, String>>> logout(
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        userAuthService.logout(httpServletRequest, httpServletResponse);

        Map<String, String> body = Map.of(
            "message", "로그아웃이 완료되었습니다.",
            "loggedOutAt", LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(BaseResponse.ok(body));
    }
}
