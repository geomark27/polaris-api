package com.azenticsys.polaris.user.service;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.user.dto.CreateUserRequest;
import com.azenticsys.polaris.user.dto.UpdateUserRequest;
import com.azenticsys.polaris.user.dto.UserFilter;
import com.azenticsys.polaris.user.dto.UserResponse;
import com.azenticsys.polaris.user.entity.User;
import com.azenticsys.polaris.user.repository.UserRepository;
import com.azenticsys.polaris.user.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .isActive(true)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return UserResponse.from(getActiveUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(PageQuery pageQuery, UserFilter filter) {
        return PageResponse.from(
                userRepository.findAll(
                        UserSpecification.withFilter(filter),
                        pageQuery.toPageable()
                ).map(UserResponse::from)
        );
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = getActiveUser(id);

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("Username already taken: " + request.username());
            }
            user.setUsername(request.username());
        }

        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        User user = getActiveUser(id);
        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);
        userRepository.save(user);
    }

    private User getActiveUser(UUID id) {
        return userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
}
