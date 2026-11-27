package com.fundoonotes.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Validates that a local date-time is in the future in Asia/Kolkata.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@Documented
@Constraint(validatedBy = FutureKolkataValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureKolkata {

    String message() default "Reminder date and time must be in the future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
