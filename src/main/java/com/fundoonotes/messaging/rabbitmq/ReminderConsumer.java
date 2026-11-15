
package com.fundoonotes.messaging.rabbitmq;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Consumer
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Listens to the reminder queue and processes incoming
 * reminder requests asynchronously.
 */
@Component
public class ReminderConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderConsumer.class);

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receiveReminder(Map<String, Object> message) {

        logger.info(
                "Received reminder event: type={}, noteId={}, userId={}, reminderAt={}",
                message.get("eventType"),
                message.get("noteId"),
                message.get("userId"),
                message.get("reminderAt")
        );
    }
}
