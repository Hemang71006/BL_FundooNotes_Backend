package com.fundoonotes.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Time
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Defines the timezone used for reminder API wall-clock values.
 * Keeps scheduling and delivery timestamps consistent across server hosts.
 */
public final class ReminderTime {

    public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private ReminderTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }
}
