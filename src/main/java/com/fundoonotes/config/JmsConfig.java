
package com.fundoonotes.config;

import jakarta.jms.Queue;

import org.apache.activemq.command.ActiveMQQueue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : JMS Messaging - Configuration
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Configures JMS messaging for the Fundoo Notes application.
 * Enables JMS listeners and creates the user registration queue.
 */

@Configuration
@EnableJms
public class JmsConfig {

    @Value("${app.jms.queue.user-registration}")
    private String userRegistrationQueueName;

    @Bean
    public Queue userRegistrationQueue() {

        return new ActiveMQQueue(userRegistrationQueueName);
    }
}
