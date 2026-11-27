
package com.fundoonotes.messaging.rabbitmq;

import com.fundoonotes.event.ReminderEvent;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Producer Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests reminder event publishing through RabbitTemplate.
 * Verifies the exchange, routing key, and message contents.
 */
@ExtendWith(MockitoExtension.class)
class ReminderProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ReminderProducer reminderProducer;

    @BeforeEach
    void setUp() {
        reminderProducer = new ReminderProducer(
                rabbitTemplate,
                "fundoo.notes.exchange",
                "reminder.requested"
        );
    }

    @Test
    void shouldPublishReminderEvent() {

        ReminderEvent event = new ReminderEvent(25L);

        reminderProducer.publishReminder(event);

        ArgumentCaptor<Object> captor =
                ArgumentCaptor.forClass(Object.class);

        verify(rabbitTemplate).convertAndSend(
                eq("fundoo.notes.exchange"),
                eq("reminder.requested"),
                captor.capture()
        );

        Map<?, ?> message = (Map<?, ?>) captor.getValue();

        assertEquals(25L, message.get("reminderId"));
        assertEquals("REMINDER_REQUESTED", message.get("eventType"));
    }
}
