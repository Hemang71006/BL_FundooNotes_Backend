package com.fundoonotes.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.annotation.EnableCaching;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Configures Redis templates and immediate-write Spring caching.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@Configuration
@EnableCaching
public class RedisConfig {

    // Creates a Redis template that stores keys and values as strings.
    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();

        // Connects the template to the configured Redis server.
        template.setConnectionFactory(connectionFactory);

        // Uses string serialization for Redis keys.
        template.setKeySerializer(new StringRedisSerializer());

        // Uses string serialization for Redis values.
        template.setValueSerializer(new StringRedisSerializer());

        // Uses string serialization for hash keys.
        template.setHashKeySerializer(new StringRedisSerializer());

        // Uses string serialization for hash values.
        template.setHashValueSerializer(new StringRedisSerializer());

        // Applies the serializer settings.
        template.afterPropertiesSet();

        return template;
    }

    // Creates Redis as the backend for Spring's cache abstraction.
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        // Registers Java time support for cached DTOs with LocalDateTime fields.
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(new ParameterNamesModule())
                .build();

        GenericJackson2JsonRedisSerializer valueSerializer =
                GenericJackson2JsonRedisSerializer.builder()
                        .objectMapper(objectMapper)
                        .defaultTyping(true)
                        .build();

        // Configures cache entries to expire after ten minutes.
        RedisCacheConfiguration cacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(valueSerializer)
                        );

        // Makes cache writes visible before put/evict/clear methods return.
        RedisCacheWriter cacheWriter = RedisCacheWriter.create(
                connectionFactory,
                configurer -> configurer.immediateWrites()
        );

        // Builds the Redis cache manager with the configured settings.
        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}