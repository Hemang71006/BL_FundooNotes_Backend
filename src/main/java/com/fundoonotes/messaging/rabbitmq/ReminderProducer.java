
package com.fundoonotes.messaging.rabbitmq;

import com.fundoonotes.event.ReminderEvent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Producer
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Publishes reminder events to RabbitMQ.
 * Sends messages only after the database transaction commits.
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
                "noteId", event.noteId(),
                "userId", event.userId(),
                "userEmail", event.userEmail(),
                "noteTitle", event.noteTitle(),
                "reminderAt", event.reminderAt().toString(),
                "eventType", "REMINDER_REQUESTED"
        );

        rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                message
        );

        logger.info(
                "Published reminder event: noteId={}, userId={}",
                event.noteId(),
                event.userId()
        );
    }
}
