
package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Repository Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests NoteRepository database operations.
 * Verifies note ownership and note status queries.
 */

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setFirstName("Hemang");
        user.setLastName("Test");
        user.setEmail("note-test@example.com");
        user.setPassword("encoded-test-password");

        user = userRepository.save(user);
    }

    @Test
    void shouldSaveNoteForUser() {

        Note note = new Note();
        note.setTitle("Study Java");
        note.setDescription("Learn JPA");
        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        assertNotNull(savedNote.getId());
        assertEquals("Study Java", savedNote.getTitle());
        assertEquals(user.getId(), savedNote.getUser().getId());
    }

    @Test
    void shouldFindActiveNotesOfUser() {

        Note note = new Note();
        note.setTitle("Active note");
        note.setUser(user);

        noteRepository.save(note);

        List<Note> notes = noteRepository
                .findByUser_IdAndArchivedFalseAndTrashedFalseOrderByUpdatedAtDesc(
                        user.getId()
                );

        assertEquals(1, notes.size());
        assertEquals("Active note", notes.get(0).getTitle());
    }

    @Test
    void shouldFindArchivedNotesOfUser() {

        Note note = new Note();
        note.setTitle("Archived note");
        note.setArchived(true);
        note.setUser(user);

        noteRepository.save(note);

        List<Note> notes = noteRepository
                .findByUser_IdAndArchivedTrueAndTrashedFalseOrderByUpdatedAtDesc(
                        user.getId()
                );

        assertEquals(1, notes.size());
        assertTrue(notes.get(0).isArchived());
    }

    @Test
    void shouldFindTrashedNotesOfUser() {

        Note note = new Note();
        note.setTitle("Trashed note");
        note.setTrashed(true);
        note.setUser(user);

        noteRepository.save(note);

        List<Note> notes = noteRepository
                .findByUser_IdAndTrashedTrueOrderByUpdatedAtDesc(
                        user.getId()
                );

        assertEquals(1, notes.size());
        assertTrue(notes.get(0).isTrashed());
    }

    @Test
    void shouldFindNoteByIdAndUserId() {

        Note note = new Note();
        note.setTitle("Owned note");
        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        var result = noteRepository.findByIdAndUser_Id(
                savedNote.getId(),
                user.getId()
        );

        assertTrue(result.isPresent());
        assertEquals("Owned note", result.get().getTitle());
    }

    @Test
    void shouldNotFindNoteForDifferentUser() {

        Note note = new Note();
        note.setTitle("Private note");
        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        var result = noteRepository.findByIdAndUser_Id(
                savedNote.getId(),
                user.getId() + 100
        );

        assertTrue(result.isEmpty());
    }
}
