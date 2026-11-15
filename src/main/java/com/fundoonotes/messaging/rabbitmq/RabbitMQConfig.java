
package com.fundoonotes.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Configuration
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Configures RabbitMQ messaging infrastructure.
 * Creates a durable queue, direct exchange, and binding
 * using the configured routing key.
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public DirectExchange reminderExchange(
            @Value("${app.rabbitmq.exchange}") String exchangeName) {

        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue reminderQueue(
            @Value("${app.rabbitmq.queue}") String queueName) {

        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding reminderBinding(
            Queue reminderQueue,
            DirectExchange reminderExchange,
            @Value("${app.rabbitmq.routing-key}") String routingKey) {

        return BindingBuilder
                .bind(reminderQueue)
                .to(reminderExchange)
                .with(routingKey);
    }
}
