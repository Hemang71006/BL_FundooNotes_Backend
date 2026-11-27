package com.fundoonotes.dto.request;

import com.fundoonotes.util.ReminderTime;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies reminder validation uses Asia/Kolkata consistently.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
class ReminderRequestValidationTest {

    @Test
    void shouldValidateAgainstKolkataWhenJvmUsesAnotherZone() {

        TimeZone originalTimeZone = TimeZone.getDefault();

        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            try (ValidatorFactory factory =
                         Validation.buildDefaultValidatorFactory()) {

                Validator validator = factory.getValidator();

                ReminderRequest pastRequest = new ReminderRequest();
                pastRequest.setReminderAt(
                        ReminderTime.now().minusMinutes(30));

                Set<ConstraintViolation<ReminderRequest>> violations =
                        validator.validate(pastRequest);

                assertEquals(1, violations.size());
                assertEquals(
                        "reminderAt",
                        violations.iterator().next()
                                .getPropertyPath().toString());
                assertEquals(
                        "Reminder date and time must be in the future",
                        violations.iterator().next().getMessage());

                ReminderRequest futureRequest = new ReminderRequest();
                futureRequest.setReminderAt(
                        ReminderTime.now().plusMinutes(30));

                assertTrue(validator.validate(futureRequest).isEmpty());
            }
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }
}
