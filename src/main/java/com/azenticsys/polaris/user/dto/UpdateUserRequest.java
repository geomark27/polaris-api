package com.azenticsys.polaris.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Pattern(
                regexp = "^\\+?[0-9\\s\\-().]{7,20}$",
                message = "Phone number format is invalid"
        )
        String phoneNumber
) {}
