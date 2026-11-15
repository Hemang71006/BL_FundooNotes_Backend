
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.event.ReminderEvent;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Reminder Service Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests reminder request validation and event publishing.
 * Verifies note ownership and rejects invalid reminder requests.
 */
@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ReminderServiceImpl reminderService;

    private User user;
    private Note note;

    @BeforeEach
    void setUp() {

        reminderService = new ReminderServiceImpl(
                noteRepository,
                userRepository,
                eventPublisher
        );

        user = new User();
        user.setId(5L);
        user.setEmail("test@example.com");

        note = new Note();
        note.setId(15L);
        note.setTitle("Test Note");
        note.setTrashed(false);
    }

    @Test
    void shouldPublishReminderEventForValidRequest() {

        LocalDateTime reminderAt = LocalDateTime.now().plusDays(1);

        ReminderRequest request = new ReminderRequest();
        request.setReminderAt(reminderAt);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser_Id(15L, 5L))
                .thenReturn(Optional.of(note));

        reminderService.requestReminder(
                15L,
                request,
                "test@example.com"
        );

        ArgumentCaptor<ReminderEvent> captor =
                ArgumentCaptor.forClass(ReminderEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        ReminderEvent event = captor.getValue();

        assertEquals(15L, event.noteId());
        assertEquals(5L, event.userId());
        assertEquals("test@example.com", event.userEmail());
        assertEquals("Test Note", event.noteTitle());
        assertEquals(reminderAt, event.reminderAt());
    }

    @Test
    void shouldRejectReminderForUnknownUser() {

        ReminderRequest request = new ReminderRequest();
        request.setReminderAt(LocalDateTime.now().plusDays(1));

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.requestReminder(
                        15L,
                        request,
                        "unknown@example.com"
                )
        );

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldRejectReminderForNoteNotOwnedByUser() {

        ReminderRequest request = new ReminderRequest();
        request.setReminderAt(LocalDateTime.now().plusDays(1));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser_Id(15L, 5L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.requestReminder(
                        15L,
                        request,
                        "test@example.com"
                )
        );

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldRejectReminderForTrashedNote() {

        ReminderRequest request = new ReminderRequest();
        request.setReminderAt(LocalDateTime.now().plusDays(1));

        note.setTrashed(true);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser_Id(15L, 5L))
                .thenReturn(Optional.of(note));

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.requestReminder(
                        15L,
                        request,
                        "test@example.com"
                )
        );

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldRejectPastReminderTime() {

        ReminderRequest request = new ReminderRequest();
        request.setReminderAt(LocalDateTime.now().minusDays(1));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser_Id(15L, 5L))
                .thenReturn(Optional.of(note));

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.requestReminder(
                        15L,
                        request,
                        "test@example.com"
                )
        );

        verify(eventPublisher, never()).publishEvent(any());
    }
}
