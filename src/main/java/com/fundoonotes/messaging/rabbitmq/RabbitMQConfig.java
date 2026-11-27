
package com.fundoonotes.messaging.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Configures RabbitMQ infrastructure and JSON message conversion.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public MessageConverter rabbitMessageConverter() {

        return new JacksonJsonMessageConverter();
    }

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
