package com.fundoonotes.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.annotation.EnableCaching;

// Configures Redis operations and enables Spring caching.
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
                                        .fromSerializer(
                                                new GenericJackson2JsonRedisSerializer()
                                        )
                        );

        // Builds the Redis cache manager with the configured settings.
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}