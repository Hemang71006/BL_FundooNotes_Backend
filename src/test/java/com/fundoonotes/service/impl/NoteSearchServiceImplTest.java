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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Note Search - Service Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests keyword and label-based note searching.
 * Verifies that search queries use the authenticated user's ID.
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

        when(noteRepository.searchActiveNotes(1L, "java", null))
                .thenReturn(List.of());

        List<NoteResponse> result = noteSearchService.searchNotes(
                "java", null, "test@example.com");

        assertEquals(0, result.size());

        verify(noteRepository).searchActiveNotes(1L, "java", null);
    }

    @Test
    void searchByLabel() {

        when(labelRepository.existsByIdAndUser_Id(10L, 1L))
                .thenReturn(true);

        when(noteRepository.searchActiveNotes(1L, null, 10L))
                .thenReturn(List.of());

        List<NoteResponse> result = noteSearchService.searchNotes(
                null, 10L, "test@example.com");

        assertEquals(0, result.size());

        verify(noteRepository).searchActiveNotes(1L, null, 10L);
    }

    @Test
    void searchByKeywordAndLabel() {

        when(labelRepository.existsByIdAndUser_Id(10L, 1L))
                .thenReturn(true);

        when(noteRepository.searchActiveNotes(1L, "java", 10L))
                .thenReturn(List.of());

        List<NoteResponse> result = noteSearchService.searchNotes(
                "java", 10L, "test@example.com");

        assertEquals(0, result.size());

        verify(noteRepository).searchActiveNotes(1L, "java", 10L);
    }
}