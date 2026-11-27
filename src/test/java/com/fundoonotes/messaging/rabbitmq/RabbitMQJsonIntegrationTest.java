package com.fundoonotes.messaging.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies JSON reminder messages through a real RabbitMQ broker.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@SpringBootTest
class RabbitMQJsonIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Test
    void shouldRoundTripReminderWakeUpAsJson() {

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Queue queue = new Queue("fundoo.test.json." + suffix, true, false, false);
        DirectExchange exchange =
                new DirectExchange("fundoo.test.exchange." + suffix, false, true);

        try {
            rabbitAdmin.declareExchange(exchange);
            rabbitAdmin.declareQueue(queue);
            rabbitAdmin.declareBinding(
                    BindingBuilder.bind(queue)
                            .to(exchange)
                            .with("test.key"));

            Map<String, Object> payload = Map.of(
                    "reminderId", 25L,
                    "eventType", "REMINDER_REQUESTED"
            );

            rabbitTemplate.convertAndSend(
                    exchange.getName(),
                    "test.key",
                    payload
            );

            Message message =
                    rabbitTemplate.receive(queue.getName(), 10_000);

            assertNotNull(message);
            assertEquals(
                    MessageProperties.CONTENT_TYPE_JSON,
                    message.getMessageProperties().getContentType());

            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            assertFalse(body.contains("CollSer"));
            assertFalse(body.contains("com.fundoonotes"));

            Object converted =
                    rabbitTemplate.getMessageConverter().fromMessage(message);
            assertNotNull(converted);

            @SuppressWarnings("unchecked")
            Map<String, Object> convertedMap = (Map<String, Object>) converted;

            assertEquals(
                    Set.of("reminderId", "eventType"),
                    convertedMap.keySet());
            assertEquals(25, convertedMap.get("reminderId"));
            assertEquals(
                    "REMINDER_REQUESTED",
                    convertedMap.get("eventType"));
        } finally {
            rabbitAdmin.deleteQueue(queue.getName());
            rabbitAdmin.deleteExchange(exchange.getName());
        }
    }
}
