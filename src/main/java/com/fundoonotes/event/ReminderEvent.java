
package com.fundoonotes.event;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Event
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Identifies a persisted reminder for asynchronous processing.
 * Personal data and scheduling details remain in MySQL.
 */
public record ReminderEvent(
        Long reminderId
) {
}
