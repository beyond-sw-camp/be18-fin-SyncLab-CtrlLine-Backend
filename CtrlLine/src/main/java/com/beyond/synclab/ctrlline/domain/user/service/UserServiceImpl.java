package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.common.exception.AppException;
import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import com.beyond.synclab.ctrlline.domain.user.errorcode.UserErrorCode;
import com.beyond.synclab.ctrlline.domain.user.repository.UserRepository;
import com.beyond.synclab.ctrlline.domain.user.spec.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getUserList(UserSearchCommand command, Pageable pageable) {
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
            .map(UserResponseDto::fromEntity);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

        return UserResponseDto.fromEntity(user);
    }


}
