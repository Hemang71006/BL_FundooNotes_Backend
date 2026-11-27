package com.fundoonotes.dto.request;

import com.fundoonotes.validation.FutureKolkata;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Carries a reminder date-time validated in Asia/Kolkata.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@Getter
@Setter
@NoArgsConstructor
public class ReminderRequest {

    @NotNull(message = "Reminder date and time are required")
    @FutureKolkata
    private LocalDateTime reminderAt;
}
