
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

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Redis Cache Integration Testing
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests Redis cache storage, retrieval, and eviction.
 * Verifies that the configured CacheManager works correctly.
 */
@SpringBootTest
class RedisCacheIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    /**
     * Verifies that a value can be stored and retrieved
     * from the configured Redis cache.
     */
    @Test
    void shouldStoreAndRetrieveCachedValue() {

        Cache cache = cacheManager.getCache("feature12TestCache");

        assertNotNull(cache, "Test cache should exist");

        String key = UUID.randomUUID().toString();

        try {
            cache.put(key, "Fundoo Notes");

            Cache.ValueWrapper cachedValue = cache.get(key);

            assertNotNull(cachedValue, "Cached value should exist");

            assertEquals("Fundoo Notes", cachedValue.get());
        } finally {
            cache.evict(key);
        }
    }

    /**
     * Verifies that a cached value can be removed
     * successfully from Redis.
     */
    @Test
    void shouldEvictCachedValue() {

        Cache cache = cacheManager.getCache("feature12TestCache");

        assertNotNull(cache);

        String key = UUID.randomUUID().toString();

        cache.put(key, "Temporary Note");

        cache.evict(key);

        assertNull(cache.get(key));
    }

    /**
     * Verifies that all expected note caches
     * are available through the CacheManager.
     */
    @Test
    void shouldCreateNoteCaches() {

        assertNotNull(cacheManager.getCache("activeNotes"));
        assertNotNull(cacheManager.getCache("archivedNotes"));
        assertNotNull(cacheManager.getCache("trashedNotes"));
        assertNotNull(cacheManager.getCache("noteById"));
    }
}
