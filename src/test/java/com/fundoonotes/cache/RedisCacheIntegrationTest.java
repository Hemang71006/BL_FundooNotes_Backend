package com.fundoonotes.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

// Tests Redis cache storage, retrieval, and eviction.
@SpringBootTest
class RedisCacheIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    // Verifies that a value can be stored and retrieved from Redis cache.
    @Test
    void shouldStoreAndRetrieveCachedValue() {

        Cache cache = cacheManager.getCache("feature12TestCache");

        assertNotNull(cache);

        String key = UUID.randomUUID().toString();

        cache.put(key, "Fundoo Notes");

        String result = cache.get(key, String.class);

        assertEquals("Fundoo Notes", result);

        cache.evict(key);
    }

    // Verifies that a cached value can be removed.
    @Test
    void shouldEvictCachedValue() {

        Cache cache = cacheManager.getCache("feature12TestCache");

        assertNotNull(cache);

        String key = UUID.randomUUID().toString();

        cache.put(key, "Temporary Note");

        cache.evict(key);

        assertNull(cache.get(key));
    }

    // Verifies that the cache manager provides the expected note caches.
    @Test
    void shouldCreateNoteCaches() {

        assertNotNull(cacheManager.getCache("activeNotes"));
        assertNotNull(cacheManager.getCache("archivedNotes"));
        assertNotNull(cacheManager.getCache("trashedNotes"));
        assertNotNull(cacheManager.getCache("noteById"));
    }
}