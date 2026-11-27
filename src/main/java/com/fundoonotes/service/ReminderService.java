package com.fundoonotes.service;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.dto.response.ReminderResponse;

import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Service
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Defines authenticated reminder management operations.
 * Supports durable scheduling, ownership checks, and note lifecycle handling.
 */
public interface ReminderService {

    ReminderResponse createReminder(
            Long noteId,
            ReminderRequest request,
            String email
    );

    List<ReminderResponse> getReminders(String email);

    List<ReminderResponse> getUpcomingReminders(String email);

    List<ReminderResponse> getNoteReminders(Long noteId, String email);

    ReminderResponse updateReminder(
            Long reminderId,
            ReminderRequest request,
            String email
    );

    ReminderResponse cancelReminder(Long reminderId, String email);

    ReminderResponse completeReminder(Long reminderId, String email);

    void deleteReminder(Long reminderId, String email);

    void cancelActiveForNote(Long noteId, Long userId);

    void deleteForNote(Long noteId, Long userId);
}
