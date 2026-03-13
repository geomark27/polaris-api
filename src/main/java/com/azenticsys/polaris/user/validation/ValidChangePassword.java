package com.azenticsys.polaris.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ChangePasswordRequestValidator.class)
@Documented
public @interface ValidChangePassword {
    String message() default "Invalid password change request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default{};
}
