package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;

import jakarta.persistence.EntityManager;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Excel Item Processor
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Validates Excel records before saving them.
 * Converts valid NoteExcelRow objects into Note entities.
 */
@Component
public class NoteItemProcessor
        implements ItemProcessor<NoteExcelRow, Note> {

    private final EntityManager entityManager;

    /**
     * Initializes the processor with EntityManager.
     */
    public NoteItemProcessor(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Validates and converts one Excel record into a Note entity.
     */
    @Override
    public Note process(NoteExcelRow row) {

        // Validate the user ID.
        if (row.getUserId() == null || row.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        // Validate the title.
        if (row.getTitle() == null || row.getTitle().isBlank()) {
            throw new IllegalArgumentException("Note title is required");
        }

        // Validate the title length.
        if (row.getTitle().length() > 255) {
            throw new IllegalArgumentException(
                    "Title cannot exceed 255 characters");
        }

        // Check whether the user exists.
        User user = entityManager.find(User.class, row.getUserId());

        if (user == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        // Create a new Note entity.
        Note note = new Note();

        note.setTitle(row.getTitle().trim());
        note.setDescription(row.getDescription());
        note.setPinned(row.isPinned());
        note.setArchived(row.isArchived());
        note.setTrashed(row.isTrashed());
        note.setUser(user);

        return note;
    }
}