package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies imported note ownership and field validation.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
class NoteItemProcessorTest {

    @Test
    void assignsImportedNoteToAuthenticatedUserInsteadOfSpreadsheetUser() {
        EntityManager entityManager = mock(EntityManager.class);
        User authenticatedUser = new User();
        authenticatedUser.setId(7L);
        when(entityManager.find(User.class, 7L)).thenReturn(authenticatedUser);

        NoteExcelRow row = new NoteExcelRow();
        row.setUserId(99L);
        row.setTitle("Imported note");

        Note note = new NoteItemProcessor(entityManager, 7L).process(row);

        assertSame(authenticatedUser, note.getUser());
        verify(entityManager).find(User.class, 7L);
    }

    @Test
    void rejectsImportWithoutAuthenticatedUser() {
        NoteExcelRow row = new NoteExcelRow();
        row.setTitle("Imported note");

        assertThrows(IllegalArgumentException.class, () ->
                new NoteItemProcessor(mock(EntityManager.class), null)
                        .process(row));
    }

    @Test
    void rejectsDescriptionLongerThanNoteApiLimit() {
        NoteExcelRow row = new NoteExcelRow();
        row.setTitle("Imported note");
        row.setDescription("x".repeat(5001));

        assertThrows(IllegalArgumentException.class, () ->
                new NoteItemProcessor(mock(EntityManager.class), 7L)
                        .process(row));
    }
}
