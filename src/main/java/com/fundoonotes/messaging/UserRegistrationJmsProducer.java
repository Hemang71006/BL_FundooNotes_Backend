
package com.fundoonotes.messaging;

import com.fundoonotes.event.UserRegisteredEvent;

import jakarta.jms.Queue;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : JMS Messaging - Producer
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Publishes user registration events to the ActiveMQ queue.
 * Sends the message only after the database transaction commits.
 */

@Component
public class UserRegistrationJmsProducer {

    private static final Logger logger =
            LoggerFactory.getLogger(UserRegistrationJmsProducer.class);

    private final JmsTemplate jmsTemplate;

    private final Queue userRegistrationQueue;

    public UserRegistrationJmsProducer(
            JmsTemplate jmsTemplate,
            Queue userRegistrationQueue) {

        this.jmsTemplate = jmsTemplate;
        this.userRegistrationQueue = userRegistrationQueue;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishRegistrationEvent(UserRegisteredEvent event) {

        Map<String, Object> message = Map.of(
                "userId", event.userId(),
                "eventType", "USER_REGISTERED",
                "registeredAt", event.registeredAt().toString()
        );

        jmsTemplate.convertAndSend(
                userRegistrationQueue,
                message
        );

        logger.info(
                "Published user registration event: userId={}",
                event.userId()
        );
    }
}
