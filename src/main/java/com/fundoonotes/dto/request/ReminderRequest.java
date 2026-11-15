
package com.fundoonotes.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Reminder Request DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Holds the reminder date and time submitted by the user.
 * Validates that the requested reminder time is in the future.
 */
@Getter
@Setter
@NoArgsConstructor
public class ReminderRequest {

    @NotNull(message = "Reminder date and time are required")
    @Future(message = "Reminder date and time must be in the future")
    private LocalDateTime reminderAt;
}
