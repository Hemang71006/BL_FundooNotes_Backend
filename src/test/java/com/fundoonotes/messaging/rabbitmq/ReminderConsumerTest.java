package com.fundoonotes.messaging.rabbitmq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReminderConsumerTest {

    private TaskScheduler taskScheduler;
    private JavaMailSender mailSender;

    private ReminderConsumer consumer;

    @BeforeEach
    void setUp() {

        taskScheduler = mock(TaskScheduler.class);
        mailSender = mock(JavaMailSender.class);

        consumer = new ReminderConsumer(
                taskScheduler,
                mailSender,
                "noreply@example.com"
        );
    }

    @Test
    void shouldScheduleReminderMessage() {

        Map<String, Object> message = new HashMap<>();

        message.put("noteId", 15L);
        message.put("userId", 5L);
        message.put("userEmail", "test@example.com");
        message.put("noteTitle", "Test Note");
        message.put(
                "reminderAt",
                Instant.now().plusSeconds(3600)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
                        .toString()
        );
        message.put("eventType", "REMINDER_REQUESTED");

        assertDoesNotThrow(
                () -> consumer.receiveReminder(message)
        );

        verify(taskScheduler).schedule(
                any(Runnable.class),
                any(Instant.class)
        );
    }
}