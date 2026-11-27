package com.fundoonotes.messaging.rabbitmq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fundoonotes.service.impl.ReminderDeliveryService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReminderConsumerTest {

    private ReminderDeliveryService reminderDeliveryService;
    private ReminderConsumer consumer;

    @BeforeEach
    void setUp() {

        reminderDeliveryService = mock(ReminderDeliveryService.class);
        consumer = new ReminderConsumer(reminderDeliveryService);
    }

    @Test
    void shouldScheduleReminderMessage() {

        Map<String, Object> message = new HashMap<>();

        message.put("reminderId", 25L);
        message.put("eventType", "REMINDER_REQUESTED");

        consumer.receiveReminder(message);

        verify(reminderDeliveryService).processReminder(eq(25L));
    }
}