
package com.fundoonotes.messaging;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : JMS Messaging - Consumer Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the user registration JMS consumer.
 * Verifies that a registration message can be processed.
 */

class UserRegistrationJmsConsumerTest {

    private final UserRegistrationJmsConsumer consumer =
            new UserRegistrationJmsConsumer();

    @Test
    void shouldProcessRegistrationMessage() {

        Map<String, Object> message = Map.of(
                "userId", 10L,
                "eventType", "USER_REGISTERED",
                "registeredAt", "2026-11-15T10:30:00"
        );

        assertDoesNotThrow(
                () -> consumer.receiveRegistrationMessage(message)
        );
    }
}
