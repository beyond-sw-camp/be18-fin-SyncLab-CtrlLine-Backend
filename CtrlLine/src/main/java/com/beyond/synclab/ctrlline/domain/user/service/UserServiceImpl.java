package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.user.dto.UserListResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateMeRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import com.beyond.synclab.ctrlline.domain.user.spec.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserListResponseDto> getUserList(UserSearchCommand command, Pageable pageable) {
        Specification<Users> spec = Specification.allOf(
            UserSpecification.userDepartmentEquals(command.userDepartment()),
            UserSpecification.userStatusEquals(command.userStatus()),
            UserSpecification.userRoleEquals(command.userRole()),
            UserSpecification.userPositionEquals(command.userPosition()),
            UserSpecification.userPhoneNumberContains(command.userPhoneNumber()),
            UserSpecification.userEmailContains(command.userEmail()),
            UserSpecification.userHiredDateAfter(command.hiredDate()),
            UserSpecification.userTerminationDateBefore(command.terminationDate())
        );

        return userRepository.findAll(spec, pageable)
            .map(UserListResponseDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

        return UserResponseDto.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserById(UserUpdateRequestDto dto, Long userId) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

        user.update(dto);

        userRepository.save(user);

        return UserResponseDto.fromEntity(user);
    }

    private void validatePassword(UserUpdateMeRequestDto dto) {
        String pw = dto.getUserPassword();
        String pwConfirm = dto.getUserPasswordConfirm();

        // 비밀번호 필드가 비어있으면 검증 스킵
        if (pw == null || pw.isBlank()) return;

        if (!pw.equals(pwConfirm)) {
            throw new AppException(UserErrorCode.PASSWORD_MISMATCH);
        }
    }

    private String encodePassword(String password) {
        if (password == null || password.isBlank()) return null;
        return passwordEncoder.encode(password);
    }

    @Override
    @Transactional
    public UserResponseDto updateMyInfo(UserUpdateMeRequestDto dto, Users user) {
        // 비밀번호 검증
        validatePassword(dto);

        // 비밀번호 인코딩 처리
        String encodedPassword = encodePassword(dto.getUserPassword());
        user.update(dto, encodedPassword);
        userRepository.save(user);
        return UserResponseDto.fromEntity(user);
    }
}
