
package com.fundoonotes.messaging;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : JMS Messaging - Consumer
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Listens to the user registration queue.
 * Processes registration events independently of the REST request.
 */

@Component
public class UserRegistrationJmsConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(UserRegistrationJmsConsumer.class);

    @JmsListener(destination = "${app.jms.queue.user-registration}")
    public void receiveRegistrationMessage(Map<String, Object> message) {

        Object userId = message.get("userId");

        Object eventType = message.get("eventType");

        logger.info(
                "Consumed registration event: type={}, userId={}",
                eventType,
                userId
        );
    }
}
