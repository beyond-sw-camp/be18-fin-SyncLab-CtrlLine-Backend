package com.beyond.synclab.ctrlline.domain.user.service;

import com.beyond.synclab.ctrlline.domain.user.dto.UserListResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateMeRequestDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserResponseDto;
import com.beyond.synclab.ctrlline.domain.user.dto.SearchUserParameterDto;
import com.beyond.synclab.ctrlline.domain.user.dto.UserUpdateRequestDto;
import com.beyond.synclab.ctrlline.domain.user.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserListResponseDto> getUserList(SearchUserParameterDto command, Pageable pageable);

    UserResponseDto getUserById(Long userId);

    UserResponseDto updateUserById(UserUpdateRequestDto dto, Long userId);

    UserResponseDto updateMyInfo(UserUpdateMeRequestDto dto, Users user);

    UserResponseDto getMyInfo(Users user);
}
