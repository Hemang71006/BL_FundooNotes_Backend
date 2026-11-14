
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Service Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests note management business operations.
 * Verifies note creation, retrieval, ownership, and status changes.
 */

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User user;
    private Note note;

    private final String email = "hemang@example.com";

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setEmail(email);

        note = new Note();
        note.setId(10L);
        note.setTitle("Study Java");
        note.setDescription("Learn Spring Boot");
        note.setUser(user);
        note.setPinned(false);
        note.setArchived(false);
        note.setTrashed(false);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
    }

    @Test
    void shouldCreateNoteSuccessfully() {

        NoteRequest request = new NoteRequest();
        request.setTitle("Study Java");
        request.setDescription("Learn Spring Boot");

        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> {
                    Note savedNote = invocation.getArgument(0);
                    savedNote.setId(10L);
                    savedNote.setCreatedAt(LocalDateTime.now());
                    savedNote.setUpdatedAt(LocalDateTime.now());
                    return savedNote;
                });

        NoteResponse response = noteService.createNote(request, email);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Study Java", response.getTitle());
        assertEquals("Learn Spring Boot", response.getDescription());

        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void shouldGetAllActiveNotes() {

        when(noteRepository
                .findByUser_IdAndArchivedFalseAndTrashedFalseOrderByUpdatedAtDesc(1L))
                .thenReturn(List.of(note));

        List<NoteResponse> responses = noteService.getAllNotes(email);

        assertEquals(1, responses.size());
        assertEquals("Study Java", responses.get(0).getTitle());
        assertFalse(responses.get(0).isArchived());
        assertFalse(responses.get(0).isTrashed());
    }

    @Test
    void shouldGetNoteById() {

        when(noteRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(note));

        NoteResponse response = noteService.getNoteById(10L, email);

        assertEquals(10L, response.getId());
        assertEquals("Study Java", response.getTitle());
    }

    @Test
    void shouldThrowNotFoundWhenNoteDoesNotBelongToUser() {

        when(noteRepository.findByIdAndUser_Id(99L, 1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> noteService.getNoteById(99L, email)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldPinNote() {

        when(noteRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponse response = noteService.pinNote(10L, email);

        assertTrue(response.isPinned());
    }

    @Test
    void shouldArchiveNote() {

        when(noteRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponse response = noteService.archiveNote(10L, email);

        assertTrue(response.isArchived());
    }

    @Test
    void shouldMoveNoteToTrash() {

        when(noteRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponse response = noteService.trashNote(10L, email);

        assertTrue(response.isTrashed());
    }

    @Test
    void shouldRestoreNoteFromTrash() {

        note.setTrashed(true);

        when(noteRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(note));

        when(noteRepository.save(any(Note.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponse response = noteService.restoreNote(10L, email);

        assertFalse(response.isTrashed());
    }

    @Test
    void shouldDeleteNotePermanently() {

        when(noteRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(note));

        noteService.deleteNote(10L, email);

        verify(noteRepository).delete(note);
    }
}
