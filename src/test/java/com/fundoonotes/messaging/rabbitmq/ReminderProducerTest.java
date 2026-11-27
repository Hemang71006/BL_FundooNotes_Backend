
package com.fundoonotes.messaging.rabbitmq;

import com.fundoonotes.event.ReminderEvent;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies reminder event payloads and JSON message conversion.
 * Author       : Hemang
 * Date         : 27-11-2026
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

    @Test
    void shouldSerializeReminderMessagesAsJson() {

        MessageConverter converter =
                new RabbitMQConfig().rabbitMessageConverter();
        Message message = converter.toMessage(
                Map.of(
                        "reminderId", 25L,
                        "eventType", "REMINDER_REQUESTED"
                ),
                new MessageProperties()
        );

        assertEquals(
                MessageProperties.CONTENT_TYPE_JSON,
                message.getMessageProperties().getContentType()
        );

        Map<?, ?> convertedMessage =
                (Map<?, ?>) converter.fromMessage(message);
        assertEquals(25, convertedMessage.get("reminderId"));
        assertEquals("REMINDER_REQUESTED", convertedMessage.get("eventType"));
    }
}
