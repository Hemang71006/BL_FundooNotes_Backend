package com.fundoonotes.messaging.rabbitmq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fundoonotes.service.impl.ReminderProcessor;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReminderConsumerTest {

    private ReminderProcessor reminderProcessor;
    private ReminderConsumer consumer;

    @BeforeEach
    void setUp() {

        reminderProcessor = mock(ReminderProcessor.class);
        consumer = new ReminderConsumer(reminderProcessor);
    }

    @Test
    void shouldScheduleReminderMessage() {

        Map<String, Object> message = new HashMap<>();

        message.put("reminderId", 25L);
        message.put("eventType", "REMINDER_REQUESTED");

        consumer.receiveReminder(message);

        verify(reminderProcessor).processReminder(eq(25L));
    }
}