package com.azenticsys.polaris.user.controller;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.user.dto.CreateUserRequest;
import com.azenticsys.polaris.user.dto.UpdateUserRequest;
import com.azenticsys.polaris.user.dto.UserFilter;
import com.azenticsys.polaris.user.dto.UserResponse;
import com.azenticsys.polaris.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> findAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber
    ) {
        PageQuery pageQuery = new PageQuery(page, size, sortBy, sortDir);
        UserFilter filter   = new UserFilter(username, email, phoneNumber);
        return ResponseEntity.ok(userService.findAll(pageQuery, filter));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
