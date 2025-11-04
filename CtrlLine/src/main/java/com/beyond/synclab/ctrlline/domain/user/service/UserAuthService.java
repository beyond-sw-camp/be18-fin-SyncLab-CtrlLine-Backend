package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSignupResponseDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import com.beyond.synclab.ctrlline.security.exception.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto request) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        Users user = request.toEntity(generateEmpNo(request.getHiredDate()), passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return UserSignupResponseDto.fromEntity(user);
    }

    private String generateEmpNo(LocalDate hiredDate) {
        // 1️⃣ 입사일 기준 prefix 생성
        String prefix = String.format("%04d%02d", hiredDate.getYear(), hiredDate.getMonthValue());

        List<String> empNos = userRepository.findEmpNosByPrefix(prefix);
        int nextSeq = 1; // 기본 순번

        if (!empNos.isEmpty()) {
            String lastEmpNo = empNos.get(0); // 가장 최신
            String lastSeqStr = lastEmpNo.substring(6); // YYYYMMXXX 중 XXX
            nextSeq = Integer.parseInt(lastSeqStr) + 1;
        }

        return prefix + String.format("%03d", nextSeq);
    }
}
