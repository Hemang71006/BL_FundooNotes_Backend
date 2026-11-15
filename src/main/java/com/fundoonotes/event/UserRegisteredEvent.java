
package com.fundoonotes.event;

import java.time.LocalDateTime;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : JMS Messaging - User Registration Event
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Represents an event generated after successful user registration.
 * Carries the user ID and registration timestamp for background processing.
 */

public record UserRegisteredEvent(
        Long userId,
        LocalDateTime registeredAt
) {
}
