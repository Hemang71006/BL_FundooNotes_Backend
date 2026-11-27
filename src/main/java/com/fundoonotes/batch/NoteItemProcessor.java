package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;

import jakarta.persistence.EntityManager;

import org.springframework.batch.infrastructure.item.ItemProcessor;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Validates imported rows and assigns notes to the authenticated user.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
public class NoteItemProcessor
        implements ItemProcessor<NoteExcelRow, Note> {

    private final EntityManager entityManager;
    private final Long authenticatedUserId;

    /**
     * Initializes the processor with the authenticated import owner.
     */
    public NoteItemProcessor(
            EntityManager entityManager,
            Long authenticatedUserId) {

        this.entityManager = entityManager;
        this.authenticatedUserId = authenticatedUserId;
    }

    /**
     * Validates and converts one Excel record into a Note entity.
     */
    @Override
    public Note process(NoteExcelRow row) {

        // Validate the title.
        if (row.getTitle() == null || row.getTitle().isBlank()) {
            throw new IllegalArgumentException("Note title is required");
        }

        // Validate the title length.
        if (row.getTitle().length() > 255) {
            throw new IllegalArgumentException(
                    "Title cannot exceed 255 characters");
        }

        if (row.getDescription() != null
                && row.getDescription().length() > 5000) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 5000 characters");
        }

        // Check whether the user exists.
        User user = entityManager.find(User.class, authenticatedUserId);

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