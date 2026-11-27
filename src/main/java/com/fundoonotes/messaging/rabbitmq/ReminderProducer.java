
package com.fundoonotes.messaging.rabbitmq;

import com.fundoonotes.event.ReminderEvent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - RabbitMQ Producer
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Publishes persisted reminder identifiers to RabbitMQ after commit.
 * Database polling recovers schedules if broker publishing is unavailable.
 */
@Component
public class ReminderProducer {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public ReminderProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.exchange}") String exchangeName,
            @Value("${app.rabbitmq.routing-key}") String routingKey) {

        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishReminder(ReminderEvent event) {

        Map<String, Object> message = Map.of(
                "reminderId", event.reminderId(),
                "eventType", "REMINDER_REQUESTED"
        );

        try {
            rabbitTemplate.convertAndSend(
                    exchangeName,
                    routingKey,
                    message
            );
            logger.info(
                    "Published reminder wake-up: reminderId={}",
                    event.reminderId()
            );
        } catch (AmqpException exception) {
            logger.error(
                    "Could not publish reminder wake-up; persistent recovery will retry: reminderId={}, errorType={}",
                    event.reminderId(),
                    exception.getClass().getSimpleName()
            );
        }
    }
}
