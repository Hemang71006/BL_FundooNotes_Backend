
package com.fundoonotes.messaging.rabbitmq;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Consumer Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the reminder consumer using a sample RabbitMQ message.
 * Verifies that the consumer can process the message without errors.
 */
class ReminderConsumerTest {

    @Test
    void shouldReceiveReminderMessage() {

        ReminderConsumer consumer = new ReminderConsumer();

        Map<String, Object> message = new HashMap<>();
        message.put("noteId", 15L);
        message.put("userId", 5L);
        message.put("userEmail", "test@example.com");
        message.put("noteTitle", "Test Note");
        message.put("reminderAt", "2026-11-20T10:30");
        message.put("eventType", "REMINDER_REQUESTED");

        assertDoesNotThrow(
                () -> consumer.receiveReminder(message)
        );
    }
}
