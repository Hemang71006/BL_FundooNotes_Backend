
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Note Search - Service Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests keyword, label, and status-based note searching.
 * Verifies that searches use the authenticated user's ID.
 */

@ExtendWith(MockitoExtension.class)
class NoteSearchServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private NoteSearchServiceImpl noteSearchService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void searchByKeyword() {

        when(noteRepository.searchNotesByStatus(
                1L, "java", null, "ACTIVE"))
                .thenReturn(List.of());

        List<NoteResponse> result = noteSearchService.searchNotes(
                "java", null, null, "test@example.com");

        assertEquals(0, result.size());

        verify(noteRepository).searchNotesByStatus(
                1L, "java", null, "ACTIVE");
    }

    @Test
    void searchByLabel() {

        when(labelRepository.existsByIdAndUser_Id(10L, 1L))
                .thenReturn(true);

        when(noteRepository.searchNotesByStatus(
                1L, null, 10L, "ACTIVE"))
                .thenReturn(List.of());

        List<NoteResponse> result = noteSearchService.searchNotes(
                null, 10L, null, "test@example.com");

        assertEquals(0, result.size());

        verify(noteRepository).searchNotesByStatus(
                1L, null, 10L, "ACTIVE");
    }

    @Test
    void searchByKeywordAndLabel() {

        when(labelRepository.existsByIdAndUser_Id(10L, 1L))
                .thenReturn(true);

        when(noteRepository.searchNotesByStatus(
                1L, "java", 10L, "ACTIVE"))
                .thenReturn(List.of());

        List<NoteResponse> result = noteSearchService.searchNotes(
                "java", 10L, null, "test@example.com");

        assertEquals(0, result.size());

        verify(noteRepository).searchNotesByStatus(
                1L, "java", 10L, "ACTIVE");
    }

    @Test
    void searchPinnedNotes() {

        when(noteRepository.searchNotesByStatus(
                1L, null, null, "PINNED"))
                .thenReturn(List.of());

        noteSearchService.searchNotes(
                null, null, "PINNED", "test@example.com");

        verify(noteRepository).searchNotesByStatus(
                1L, null, null, "PINNED");
    }

    @Test
    void searchArchivedNotes() {

        when(noteRepository.searchNotesByStatus(
                1L, null, null, "ARCHIVED"))
                .thenReturn(List.of());

        noteSearchService.searchNotes(
                null, null, "ARCHIVED", "test@example.com");

        verify(noteRepository).searchNotesByStatus(
                1L, null, null, "ARCHIVED");
    }

    @Test
    void searchTrashedNotes() {

        when(noteRepository.searchNotesByStatus(
                1L, null, null, "TRASHED"))
                .thenReturn(List.of());

        noteSearchService.searchNotes(
                null, null, "TRASHED", "test@example.com");

        verify(noteRepository).searchNotesByStatus(
                1L, null, null, "TRASHED");
    }

    @Test
    void searchStatusShouldBeCaseInsensitive() {

        when(noteRepository.searchNotesByStatus(
                1L, null, null, "PINNED"))
                .thenReturn(List.of());

        noteSearchService.searchNotes(
                null, null, "pinned", "test@example.com");

        verify(noteRepository).searchNotesByStatus(
                1L, null, null, "PINNED");
    }

    @Test
    void searchShouldRejectInvalidStatus() {

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> noteSearchService.searchNotes(
                        null, null, "DELETED", "test@example.com"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void searchShouldRejectLabelOwnedByAnotherUser() {

        when(labelRepository.existsByIdAndUser_Id(10L, 1L))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> noteSearchService.searchNotes(
                        null, 10L, null, "test@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
