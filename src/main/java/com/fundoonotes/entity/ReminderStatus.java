package com.fundoonotes.entity;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Status
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Defines lifecycle states for persisted note reminders.
 * Supports delivery, retry, cancellation, and completion tracking.
 */
public enum ReminderStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    CANCELLED,
    COMPLETED
}
