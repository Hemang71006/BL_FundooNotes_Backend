package com.fundoonotes.cache;

import com.fundoonotes.dto.response.NoteResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies immediate writes, serialization and eviction in Redis.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@SpringBootTest
class RedisCacheIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldInjectConfiguredRedisCacheManager() {

        assertInstanceOf(RedisCacheManager.class, cacheManager);
    }

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

    @Test
    void shouldSerializeAndDeserializeResponseThroughRealCache() {

        Cache cache = cacheManager.getCache("noteById");

        assertNotNull(cache);

        String key = "cache-test:" + UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 11, 27, 10, 30);
        NoteResponse note = new NoteResponse(
                42L,
                "Cached note",
                "Cached description",
                true,
                false,
                false,
                now,
                now
        );

        try {
            cache.put(key, note);

            NoteResponse cached = cache.get(key, NoteResponse.class);

            assertNotNull(cached);
            assertEquals(note.getId(), cached.getId());
            assertEquals(note.getTitle(), cached.getTitle());
            assertEquals(note.getDescription(), cached.getDescription());
            assertEquals(note.isPinned(), cached.isPinned());
            assertEquals(note.getUpdatedAt(), cached.getUpdatedAt());
        } finally {
            cache.evict(key);
        }
    }

    @Test
    void shouldEvictCachedValue() {

        Cache cache = cacheManager.getCache("feature12TestCache");

        assertNotNull(cache);

        String key = UUID.randomUUID().toString();

        cache.put(key, "Temporary Note");

        cache.evict(key);

        assertNull(cache.get(key));
    }

    @Test
    void shouldClearAllEntriesFromCache() {

        Cache cache = cacheManager.getCache("feature12TestCache");

        assertNotNull(cache);

        String firstKey = "clear-first-" + UUID.randomUUID();
        String secondKey = "clear-second-" + UUID.randomUUID();

        cache.put(firstKey, "first");
        cache.put(secondKey, "second");

        cache.clear();

        assertNull(cache.get(firstKey));
        assertNull(cache.get(secondKey));
    }

    @Test
    void shouldCreateNoteCaches() {

        assertNotNull(cacheManager.getCache("activeNotes"));
        assertNotNull(cacheManager.getCache("archivedNotes"));
        assertNotNull(cacheManager.getCache("trashedNotes"));
        assertNotNull(cacheManager.getCache("noteById"));
    }
}
