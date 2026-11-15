
package com.fundoonotes.event;

import java.time.LocalDateTime;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Reminder Event
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Represents a request to process a reminder for a note.
 * Carries the note, user, and requested reminder information.
 */
public record ReminderEvent(
        Long noteId,
        Long userId,
        String userEmail,
        String noteTitle,
        LocalDateTime reminderAt
) {
}
