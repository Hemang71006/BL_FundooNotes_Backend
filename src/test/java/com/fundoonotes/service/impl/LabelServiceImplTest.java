
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.LabelRequest;
import com.fundoonotes.dto.response.LabelResponse;
import com.fundoonotes.entity.Label;
import com.fundoonotes.entity.Note;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Label Management - Service Tests
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests label CRUD and note-label association operations.
 * Verifies ownership checks and invalid note-state handling.
 */

@ExtendWith(MockitoExtension.class)
class LabelServiceImplTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LabelServiceImpl labelService;

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
    void createLabelSuccessfully() {

        LabelRequest request = new LabelRequest();
        request.setName("Work");
        request.setColor("#FF0000");

        when(labelRepository.existsByUser_IdAndNameIgnoreCase(
                1L, "Work")).thenReturn(false);

        when(labelRepository.save(any(Label.class)))
                .thenAnswer(invocation -> {
                    Label label = invocation.getArgument(0);
                    label.setId(10L);
                    return label;
                });

        LabelResponse response =
                labelService.createLabel(request, "test@example.com");

        assertEquals(10L, response.getId());
        assertEquals("Work", response.getName());
        assertEquals("#FF0000", response.getColor());
    }

    @Test
    void createLabelShouldRejectDuplicateName() {

        LabelRequest request = new LabelRequest();
        request.setName("Work");

        when(labelRepository.existsByUser_IdAndNameIgnoreCase(
                1L, "Work")).thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> labelService.createLabel(
                        request, "test@example.com"));

        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    void addLabelToNoteSuccessfully() {

        Note note = new Note();
        note.setId(5L);

        Label label = new Label();
        label.setId(10L);
        label.setName("Work");

        when(noteRepository.findByIdAndUser_Id(5L, 1L))
                .thenReturn(Optional.of(note));

        when(labelRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(label));

        LabelResponse response = labelService.addLabelToNote(
                5L, 10L, "test@example.com");

        assertEquals(10L, response.getId());
        assertEquals("Work", response.getName());
        assertEquals(1, note.getLabels().size());

        verify(noteRepository).save(note);
    }

    @Test
    void addLabelShouldRejectAnotherUsersLabel() {

        Note note = new Note();
        note.setId(5L);

        when(noteRepository.findByIdAndUser_Id(5L, 1L))
                .thenReturn(Optional.of(note));

        when(labelRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> labelService.addLabelToNote(
                        5L, 10L, "test@example.com"));

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void addLabelShouldRejectTrashedNote() {

        Note note = new Note();
        note.setId(5L);
        note.setTrashed(true);

        when(noteRepository.findByIdAndUser_Id(5L, 1L))
                .thenReturn(Optional.of(note));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> labelService.addLabelToNote(
                        5L, 10L, "test@example.com"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(labelRepository, never())
                .findByIdAndUser_Id(10L, 1L);

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void removeLabelFromNoteSuccessfully() {

        Note note = new Note();
        note.setId(5L);

        Label label = new Label();
        label.setId(10L);
        label.setName("Work");

        note.getLabels().add(label);

        when(noteRepository.findByIdAndUser_Id(5L, 1L))
                .thenReturn(Optional.of(note));

        when(labelRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(label));

        labelService.removeLabelFromNote(
                5L, 10L, "test@example.com");

        assertEquals(0, note.getLabels().size());
        verify(noteRepository).save(note);
    }

    @Test
    void removeLabelShouldRejectAnotherUsersLabel() {

        Note note = new Note();
        note.setId(5L);

        when(noteRepository.findByIdAndUser_Id(5L, 1L))
                .thenReturn(Optional.of(note));

        when(labelRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> labelService.removeLabelFromNote(
                        5L, 10L, "test@example.com"));

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void labelOperationsShouldRejectAnotherUsersNote() {

        when(noteRepository.findByIdAndUser_Id(5L, 1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> labelService.addLabelToNote(
                        5L, 10L, "test@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(labelRepository, never())
                .findByIdAndUser_Id(10L, 1L);
    }

    @Test
    void deleteLabelShouldRemoveItsNoteAssociations() {

        Label label = new Label();
        label.setId(10L);

        Note note = new Note();
        note.setId(5L);
        note.getLabels().add(label);

        when(labelRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(label));

        when(noteRepository.findAllByUser_IdAndLabels_Id(1L, 10L))
                .thenReturn(List.of(note));

        labelService.deleteLabel(10L, "test@example.com");

        assertEquals(0, note.getLabels().size());
        verify(noteRepository).saveAll(any());
        verify(labelRepository).delete(label);
    }
}
