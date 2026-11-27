package com.fundoonotes.messaging.rabbitmq;

import com.fundoonotes.service.impl.ReminderDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - RabbitMQ Consumer
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Consumes reminder wake-up messages and processes persisted records.
 * Database scheduling remains authoritative across broker or server restarts.
 */
@Component
public class ReminderConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderConsumer.class);

    private final ReminderDeliveryService reminderDeliveryService;

    public ReminderConsumer(ReminderDeliveryService reminderDeliveryService) {
        this.reminderDeliveryService = reminderDeliveryService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveReminder(Map<String, Object> message) {

        Object reminderIdValue = message.get("reminderId");
        if (reminderIdValue == null) {
            logger.error("Discarding invalid reminder wake-up without reminderId");
            return;
        }

        try {
            reminderDeliveryService.processReminder(
                    Long.valueOf(reminderIdValue.toString())
            );
        } catch (NumberFormatException exception) {
            logger.error("Discarding invalid reminder wake-up with non-numeric reminderId");
        }
    }
}
