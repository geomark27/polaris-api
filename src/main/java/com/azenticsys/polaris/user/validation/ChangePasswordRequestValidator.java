package com.azenticsys.polaris.user.validation;

import com.azenticsys.polaris.user.dto.ChangePasswordRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChangePasswordRequestValidator implements ConstraintValidator<ValidChangePassword, ChangePasswordRequest> {
    
    @Override
    public boolean isValid(ChangePasswordRequest value, ConstraintValidatorContext context) {
        
        if (value == null) {
            return true;
        }
        
        boolean valid = true;
        context.disableDefaultConstraintViolation();
        
        String currentPassword      = value.currentPassword();
        String newPassword          = value.newPassword();
        String confirmNewPassword   = value.confirmNewPassword();
        
        if (currentPassword != null && newPassword != null && currentPassword.equals(newPassword)) {
            context.buildConstraintViolationWithTemplate(
                "New password must be different form current password"
            ).addPropertyNode("newPassword")
            .addConstraintViolation();
            
            valid = false;
        }
        
        if (newPassword != null && confirmNewPassword != null && !newPassword.equals(confirmNewPassword)) {
            context.buildConstraintViolationWithTemplate(
                "Confirm new password does not match new password"
            ).addPropertyNode("confirmNewPassword")
            .addConstraintViolation();

            valid = false;
        }
        
        return valid;
    }
    
}
