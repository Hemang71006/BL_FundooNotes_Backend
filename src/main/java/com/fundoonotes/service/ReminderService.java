
package com.fundoonotes.service;

import com.fundoonotes.dto.request.ReminderRequest;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Reminder Service Interface
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines the operation for requesting a note reminder.
 * Keeps reminder business logic separate from the controller.
 */
public interface ReminderService {

    void requestReminder(
            Long noteId,
            ReminderRequest request,
            String email
    );
}
