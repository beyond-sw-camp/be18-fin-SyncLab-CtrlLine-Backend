package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.domain.user.dto.ReissueResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserAuthService {

    UserSignupResponseDto enroll(UserSignupRequestDto request);

    void logout(HttpServletRequest request, HttpServletResponse response);

    ReissueResponseDto reissue(String refreshToken);
}
