package com.azenticsys.polaris.user.dto;

import com.azenticsys.polaris.user.validation.ValidChangePassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@ValidChangePassword
public record ChangePasswordRequest(

        @NotBlank(message = "Password is required")
        String currentPassword,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword,

        @NotBlank(message = "Password is required")
        String confirmNewPassword
) {}
