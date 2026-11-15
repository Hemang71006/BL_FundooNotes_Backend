
package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Excel Item Writer
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Saves processed Note entities into the database.
 * Invalidates note caches after a successful database commit
 * to prevent stale data after Excel imports.
 */
@Component
public class NoteItemWriter implements ItemWriter<Note> {

    private static final List<String> CACHE_NAMES = List.of(
            "activeNotes",
            "archivedNotes",
            "trashedNotes",
            "noteById"
    );

    private final NoteRepository noteRepository;
    private final CacheManager cacheManager;

    /**
     * Initializes the writer with repository and cache manager.
     */
    public NoteItemWriter(
            NoteRepository noteRepository,
            CacheManager cacheManager
    ) {
        this.noteRepository = noteRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Saves a chunk of notes and clears related caches
     * only after the database transaction commits.
     */
    @Override
    public void write(Chunk<? extends Note> chunk) {

        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        noteRepository.saveAll(chunk.getItems());

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            evictNoteCaches();
                        }
                    }
            );

        } else {
            evictNoteCaches();
        }
    }

    /**
     * Clears all note-related caches.
     */
    private void evictNoteCaches() {

        for (String cacheName : CACHE_NAMES) {

            Cache cache = cacheManager.getCache(cacheName);

            if (cache != null) {
                cache.clear();
            }
        }
    }
}
