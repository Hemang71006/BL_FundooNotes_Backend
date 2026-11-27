
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.entity.User;
import com.fundoonotes.event.ReminderEvent;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.util.ReminderTime;

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
    private ReminderRepository reminderRepository;

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
                reminderRepository,
                eventPublisher
        );

        user = new User();
        user.setId(5L);
        user.setEmail("test@example.com");

        note = new Note();
        note.setId(15L);
        note.setTitle("Test Note");
        note.setTrashed(false);
        note.setUser(user);
    }

    @Test
    void shouldPublishReminderEventForValidRequest() {

        LocalDateTime reminderAt = ReminderTime.now().plusDays(1);

        ReminderRequest request = new ReminderRequest();
        request.setReminderAt(reminderAt);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser_IdForUpdate(15L, 5L))
                .thenReturn(Optional.of(note));

        when(reminderRepository.saveAndFlush(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder savedReminder = invocation.getArgument(0);
                    savedReminder.setId(25L);
                    return savedReminder;
                });

        reminderService.createReminder(
                15L,
                request,
                "test@example.com"
        );

        ArgumentCaptor<Reminder> reminderCaptor =
                ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepository).saveAndFlush(reminderCaptor.capture());
        assertEquals(15L, reminderCaptor.getValue().getNote().getId());
        assertEquals(5L, reminderCaptor.getValue().getUser().getId());

        ArgumentCaptor<ReminderEvent> captor =
                ArgumentCaptor.forClass(ReminderEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(25L, captor.getValue().reminderId());
    }

    @Test
    void shouldRejectReminderForUnknownUser() {

        ReminderRequest request = new ReminderRequest();
        request.setReminderAt(ReminderTime.now().plusDays(1));

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.createReminder(
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
        request.setReminderAt(ReminderTime.now().plusDays(1));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser_IdForUpdate(15L, 5L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.createReminder(
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
        request.setReminderAt(ReminderTime.now().plusDays(1));

        note.setTrashed(true);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser_IdForUpdate(15L, 5L))
                .thenReturn(Optional.of(note));

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.createReminder(
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
        request.setReminderAt(ReminderTime.now().minusDays(1));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(noteRepository.findByIdAndUser_IdForUpdate(15L, 5L))
                .thenReturn(Optional.of(note));

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.createReminder(
                        15L,
                        request,
                        "test@example.com"
                )
        );

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldRescheduleFailedReminderForItsOwner() {

        Reminder reminder = new Reminder();
        reminder.setId(25L);
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setStatus(ReminderStatus.FAILED);
        reminder.setRetryCount(5);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(reminderRepository.lockByIdAndUser_Id(25L, 5L))
                .thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReminderRequest request = new ReminderRequest();
        request.setReminderAt(ReminderTime.now().plusDays(1));

        reminderService.updateReminder(25L, request, "test@example.com");

        assertEquals(ReminderStatus.PENDING, reminder.getStatus());
        assertEquals(0, reminder.getRetryCount());
        verify(eventPublisher).publishEvent(new ReminderEvent(25L));
    }

    @Test
    void shouldNotAllowAnotherUserToManageReminder() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(reminderRepository.lockByIdAndUser_Id(25L, 5L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> reminderService.cancelReminder(25L, "test@example.com")
        );

        verify(reminderRepository, never()).save(any(Reminder.class));
    }
}
