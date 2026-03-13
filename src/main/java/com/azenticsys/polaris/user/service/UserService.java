package com.azenticsys.polaris.user.service;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.user.dto.ChangePasswordRequest;
import com.azenticsys.polaris.user.dto.CreateUserRequest;
import com.azenticsys.polaris.user.dto.UpdateUserRequest;
import com.azenticsys.polaris.user.dto.UserFilter;
import com.azenticsys.polaris.user.dto.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse findById(UUID id);

    PageResponse<UserResponse> findAll(PageQuery pageQuery, UserFilter filter);

    UserResponse update(UUID id, UpdateUserRequest request);

    void softDelete(UUID id);
    
    void changePassword(String username, ChangePasswordRequest request);
}
