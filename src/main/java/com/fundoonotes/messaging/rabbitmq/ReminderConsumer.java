package com.fundoonotes.messaging.rabbitmq;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class ReminderConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderConsumer.class);

    private final TaskScheduler taskScheduler;
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public ReminderConsumer(
            TaskScheduler taskScheduler,
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromEmail) {

        this.taskScheduler = taskScheduler;
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveReminder(Map<String, Object> message) {

        Long noteId = Long.valueOf(
                message.get("noteId").toString()
        );

        String userEmail =
                message.get("userEmail").toString();

        String noteTitle =
                message.get("noteTitle").toString();

        LocalDateTime reminderAt;

        try {
            reminderAt = LocalDateTime.parse(
                    message.get("reminderAt").toString()
            );
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid reminder date in RabbitMQ message",
                    exception
            );
        }

        Instant scheduledTime = reminderAt
                .atZone(ZoneId.systemDefault())
                .toInstant();

        logger.info(
                "Received reminder: noteId={}, reminderAt={}",
                noteId,
                reminderAt
        );

        // Schedule the email for the requested reminder time.
        taskScheduler.schedule(
                () -> sendReminderEmail(userEmail, noteTitle, noteId),
                scheduledTime
        );
    }

    private void sendReminderEmail(
            String userEmail,
            String noteTitle,
            Long noteId) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Fundoo Notes - Note Reminder");

            message.setText(
                    "Hello,\n\n" +
                            "This is your scheduled reminder for the following note:\n\n" +
                            "Note: " + noteTitle + "\n" +
                            "Note ID: " + noteId + "\n\n" +
                            "Please open Fundoo Notes to view your note.\n\n" +
                            "Regards,\nFundoo Notes Team"
            );

            mailSender.send(message);

            logger.info(
                    "Reminder email sent successfully for noteId={}",
                    noteId
            );

        } catch (Exception exception) {

            logger.error(
                    "Failed to send reminder email for noteId={}",
                    noteId,
                    exception
            );
        }
    }
}