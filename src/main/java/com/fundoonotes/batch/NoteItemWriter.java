package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Excel Item Writer
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Saves processed Note entities into the MySQL database.
 * Uses chunk-based writing to save multiple records
 * in a single database operation.
 */
@Component
public class NoteItemWriter implements ItemWriter<Note> {

    private final NoteRepository noteRepository;

    /**
     * Initializes the writer with NoteRepository.
     */
    public NoteItemWriter(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Writes a chunk of processed notes into the database.
     */
    @Override
    public void write(Chunk<? extends Note> chunk) {

        noteRepository.saveAll(chunk.getItems());
    }
}