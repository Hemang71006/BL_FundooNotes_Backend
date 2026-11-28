package com.fundoonotes.cache;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtService;
import com.fundoonotes.service.NoteService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies note-list caching against real Redis and MySQL.
 * Author       : Hemang
 * Date         : 28-11-2026
 */
@SpringBootTest(properties = {
        "spring.data.redis.database=15",
        "logging.level.com.fundoonotes=WARN"
})
@AutoConfigureMockMvc
@Transactional
class NotesRedisCacheIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteService noteService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory connectionFactory;

    private RedisTemplate<String, byte[]> rawRedisTemplate;

    private String email;
    private Long noteId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String otherEmail;
    private Long otherNoteId;

    @BeforeEach
    void setUp() {

        email = "cache-" + UUID.randomUUID() + "@example.com";
        otherEmail = "cache-other-" + UUID.randomUUID() + "@example.com";

        User user = createUser(email);
        Note note = createNote(user, "Cached note");
        noteId = note.getId();
        createdAt = note.getCreatedAt();
        updatedAt = note.getUpdatedAt();

        rawRedisTemplate = new RedisTemplate<>();
        rawRedisTemplate.setConnectionFactory(connectionFactory);
        rawRedisTemplate.setKeySerializer(new StringRedisSerializer());
        rawRedisTemplate.setValueSerializer(RedisSerializer.byteArray());
        rawRedisTemplate.afterPropertiesSet();

        evictTestKeys();
    }

    @AfterEach
    void tearDown() {
        evictTestKeys();
    }

    @Test
    void repeatedAuthenticatedRequestsPopulateAndReadCachedNotes() throws Exception {

        String token = jwtService.generateToken(email);

        assertGetNotes(token, 1, "Cached note");

        Cache activeNotes = cacheManager.getCache("activeNotes");
        assertNotNull(activeNotes);
        assertNotNull(activeNotes.get(email), "First request should populate Redis");

        byte[] rawValue = rawRedisTemplate.opsForValue()
                .get("activeNotes::" + email);
        assertNotNull(rawValue);
        String storedJson = new String(rawValue, StandardCharsets.UTF_8);
        assertTrue(storedJson.contains("\"version\":\"2\""));
        assertTrue(storedJson.contains("\"type\":\"noteResponseList\""));

        // Second and later requests must read the same cached value successfully.
        for (int attempt = 2; attempt <= 5; attempt++) {
            assertGetNotes(token, 1, "Cached note");
        }

        Object cachedValue = activeNotes.get(email).get();
        List<?> cachedNotes = assertInstanceOf(List.class, cachedValue);
        assertEquals(1, cachedNotes.size());

        NoteResponse cachedNote = assertInstanceOf(
                NoteResponse.class, cachedNotes.get(0));
        assertEquals(noteId, cachedNote.getId());
        assertEquals("Cached note", cachedNote.getTitle());
        assertEquals("Cached description", cachedNote.getDescription());
        assertEquals(createdAt, cachedNote.getCreatedAt());
        assertEquals(updatedAt, cachedNote.getUpdatedAt());
        assertInstanceOf(LocalDateTime.class, cachedNote.getCreatedAt());
        assertInstanceOf(LocalDateTime.class, cachedNote.getUpdatedAt());
    }

    @Test
    void incompatibleLegacyCacheEntryIsTreatedAsCacheMiss() throws Exception {

        String legacyJson = "[{\"@class\":"
                + "\"com.fundoonotes.dto.response.NoteResponse\","
                + "\"id\":" + noteId + ","
                + "\"title\":\"Legacy cached note\","
                + "\"createdAt\":[2026,11,27,10,30],"
                + "\"updatedAt\":[2026,11,27,10,30]}]";

        rawRedisTemplate.opsForValue().set(
                "activeNotes::" + email,
                legacyJson.getBytes(StandardCharsets.UTF_8));

        String token = jwtService.generateToken(email);

        assertGetNotes(token, 1, "Cached note");

        byte[] replacement = rawRedisTemplate.opsForValue()
                .get("activeNotes::" + email);
        assertNotNull(replacement);
        assertTrue(new String(replacement, StandardCharsets.UTF_8)
                .contains("\"version\":\"2\""));
    }

    @Test
    void differentUsersDoNotShareCachedNotes() throws Exception {

        User otherUser = createUser(otherEmail);
        Note otherNote = createNote(otherUser, "Other user note");
        otherNoteId = otherNote.getId();

        String firstToken = jwtService.generateToken(email);
        String secondToken = jwtService.generateToken(otherEmail);

        assertGetNotes(firstToken, 1, "Cached note");
        assertGetNotes(secondToken, 1, "Other user note");
        assertGetNotes(firstToken, 1, "Cached note");

        Cache activeNotes = cacheManager.getCache("activeNotes");
        assertNotNull(activeNotes);
        assertNotNull(activeNotes.get(email));
        assertNotNull(activeNotes.get(otherEmail));
        assertNotEquals(
                activeNotes.get(email).get(),
                activeNotes.get(otherEmail).get());
    }

    @Test
    void noteMutationsEvictCachedNotes() throws Exception {

        String token = jwtService.generateToken(email);

        assertGetNotes(token, 1, "Cached note");
        noteService.getNoteById(noteId, email);

        assertTrue(isCached("activeNotes", email));
        assertTrue(isCached("noteById", noteId + ":" + email));

        noteService.createNote(noteRequest("New note"), email);
        assertFalseCached("activeNotes", email);

        assertGetNotes(token, 2, "New note");
        noteService.updateNote(
                noteId, noteRequest("Updated note"), email);
        assertFalseCached("activeNotes", email);
        assertFalseCached("noteById", noteId + ":" + email);

        assertGetNotes(token, 2, "Updated note");
        noteService.pinNote(noteId, email);
        assertFalseCached("activeNotes", email);

        assertGetNotes(token, 2, "Updated note");
        noteService.unpinNote(noteId, email);
        assertFalseCached("activeNotes", email);

        assertGetNotes(token, 2, "Updated note");
        noteService.archiveNote(noteId, email);
        assertFalseCached("activeNotes", email);
        assertFalseCached("noteById", noteId + ":" + email);

        noteService.unarchiveNote(noteId, email);
        assertFalseCached("activeNotes", email);
        assertFalseCached("noteById", noteId + ":" + email);

        assertGetNotes(token, 2, "Updated note");
        noteService.trashNote(noteId, email);
        assertFalseCached("activeNotes", email);
        assertFalseCached("noteById", noteId + ":" + email);

        noteService.restoreNote(noteId, email);
        assertFalseCached("activeNotes", email);
        assertFalseCached("noteById", noteId + ":" + email);

        assertGetNotes(token, 2, "Updated note");
        noteService.deleteNote(noteId, email);
        assertFalseCached("activeNotes", email);
        assertFalseCached("noteById", noteId + ":" + email);

        noteId = null;
    }

    private void assertGetNotes(
            String token, int expectedSize, String expectedTitle) throws Exception {

        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedSize)))
                .andExpect(jsonPath("$[0].title").value(expectedTitle))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
    }

    private User createUser(String userEmail) {

        User user = new User();
        user.setFirstName("Cache");
        user.setLastName("Tester");
        user.setEmail(userEmail);
        user.setPassword("encoded-password");
        return userRepository.saveAndFlush(user);
    }

    private Note createNote(User user, String title) {

        Note note = new Note();
        note.setTitle(title);
        note.setDescription("Cached description");
        note.setUser(user);
        return noteRepository.saveAndFlush(note);
    }

    private NoteRequest noteRequest(String title) {

        NoteRequest request = new NoteRequest();
        request.setTitle(title);
        request.setDescription("Cached description");
        return request;
    }

    private boolean isCached(String cacheName, Object key) {

        Cache cache = cacheManager.getCache(cacheName);
        return cache != null && cache.get(key) != null;
    }

    private void assertFalseCached(String cacheName, Object key) {

        assertNull(
                cacheManager.getCache(cacheName).get(key),
                cacheName + " should be evicted after a note mutation");
    }

    private void evictTestKeys() {

        for (String cacheName : List.of(
                "activeNotes", "archivedNotes", "trashedNotes", "noteById")) {

            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                continue;
            }

            cache.evict(email);
            cache.evict(otherEmail);

            if (noteId != null) {
                cache.evict(noteId + ":" + email);
            }
            if (otherNoteId != null) {
                cache.evict(otherNoteId + ":" + otherEmail);
            }
        }
    }
}
