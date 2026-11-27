package com.fundoonotes.validation;

import com.fundoonotes.util.ReminderTime;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Checks reminder date-times against the Asia/Kolkata clock.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
public class FutureKolkataValidator
        implements ConstraintValidator<FutureKolkata, LocalDateTime> {

    @Override
    public boolean isValid(
            LocalDateTime value,
            ConstraintValidatorContext context) {

        return value != null && value.isAfter(ReminderTime.now());
    }
}
