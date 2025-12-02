package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.domain.user.dto.UserListResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserSearchCommand;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserListResponseDto> getUserList(UserSearchCommand command, Pageable pageable);

    UserResponseDto getUserById(Long userId);

    UserResponseDto updateUserById(UserUpdateRequestDto dto, Long userId);
}
