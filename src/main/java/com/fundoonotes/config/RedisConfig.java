package com.fundoonotes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// Configures Redis key and value serialization.
@Configuration
public class RedisConfig {

    // Creates a Redis template that stores keys and values as strings.
    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();

        // Connects the template to the configured Redis server.
        template.setConnectionFactory(connectionFactory);

        // Uses readable string serialization for Redis keys.
        template.setKeySerializer(new StringRedisSerializer());

        // Uses readable string serialization for Redis values.
        template.setValueSerializer(new StringRedisSerializer());

        // Applies string serialization to hash keys.
        template.setHashKeySerializer(new StringRedisSerializer());

        // Applies string serialization to hash values.
        template.setHashValueSerializer(new StringRedisSerializer());

        // Applies all serializer settings before returning the template.
        template.afterPropertiesSet();

        return template;
    }
}