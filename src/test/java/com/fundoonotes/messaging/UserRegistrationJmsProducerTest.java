
package com.fundoonotes.messaging;

import com.fundoonotes.event.UserRegisteredEvent;

import jakarta.jms.Queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : JMS Messaging - Producer Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the user registration JMS producer.
 * Verifies that registration events are converted into queue messages.
 */

@ExtendWith(MockitoExtension.class)
class UserRegistrationJmsProducerTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Queue userRegistrationQueue;

    @InjectMocks
    private UserRegistrationJmsProducer producer;

    @Test
    void shouldPublishRegistrationEventToQueue() {

        UserRegisteredEvent event = new UserRegisteredEvent(
                10L,
                LocalDateTime.of(2026, 11, 15, 10, 30)
        );

        producer.publishRegistrationEvent(event);

        ArgumentCaptor<Object> messageCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(jmsTemplate).convertAndSend(
                eq(userRegistrationQueue),
                messageCaptor.capture()
        );

        assertInstanceOf(Map.class, messageCaptor.getValue());

        Map<?, ?> message = (Map<?, ?>) messageCaptor.getValue();

        assertEquals(10L, message.get("userId"));
        assertEquals("USER_REGISTERED", message.get("eventType"));
        assertEquals(
                "2026-11-15T10:30",
                message.get("registeredAt")
        );
    }
}
