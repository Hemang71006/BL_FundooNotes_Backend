package com.fundoonotes.dto.response;

import com.fundoonotes.entity.ReminderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Response
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Exposes reminder data without exposing user or persistence entities.
 * Provides lifecycle and notification delivery status to the frontend.
 */
@Getter
@AllArgsConstructor
public class ReminderResponse {

    private Long id;
    private Long noteId;
    private String noteTitle;
    private LocalDateTime reminderAt;
    private ReminderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;
    private int retryCount;
}
