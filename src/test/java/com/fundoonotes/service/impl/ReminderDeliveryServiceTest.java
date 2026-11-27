package com.fundoonotes.service.impl;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Delivery Tests
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Verifies owner-addressed email delivery and retry state transitions.
 * Exercises duplicate claim handling without a live SMTP server.
 */
@ExtendWith(MockitoExtension.class)
class ReminderDeliveryServiceTest {

    @Mock
    private ReminderClaimService claimService;

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private JavaMailSender mailSender;

    private ReminderDeliveryService deliveryService;
    private Reminder reminder;

    @BeforeEach
    void setUp() {

        deliveryService = new ReminderDeliveryService(
                claimService,
                reminderRepository,
                noteRepository,
                mailSender
        );
        ReflectionTestUtils.setField(deliveryService, "fromEmail", "noreply@example.com");

        User user = new User();
        user.setId(8L);
        user.setEmail("owner@example.com");

        Note note = new Note();
        note.setId(12L);
        note.setTitle("Study Java");
        note.setUser(user);

        reminder = new Reminder();
        reminder.setId(21L);
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(LocalDateTime.now().minusMinutes(1));
        reminder.setStatus(ReminderStatus.PROCESSING);
        reminder.setRetryCount(1);
    }

    @Test
    void sendsToCurrentOwnerAndMarksReminderSentAfterMailSucceeds() {

        when(claimService.claimReminder(21L)).thenReturn(true);
        when(reminderRepository.findById(21L)).thenReturn(Optional.of(reminder));
        when(noteRepository.findByIdAndUser_IdForUpdate(12L, 8L))
                .thenReturn(Optional.of(reminder.getNote()));
        when(reminderRepository.lockById(21L)).thenReturn(Optional.of(reminder));

        deliveryService.processReminder(21L);

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        assertEquals("owner@example.com", mailCaptor.getValue().getTo()[0]);
        assertEquals("Fundoo Notes - Note Reminder", mailCaptor.getValue().getSubject());
        assertEquals(ReminderStatus.SENT, reminder.getStatus());
        assertNotNull(reminder.getDeliveredAt());
        verify(reminderRepository).save(reminder);
    }

    @Test
    void schedulesBoundedRetryWhenMailDeliveryFails() {

        when(claimService.claimReminder(21L)).thenReturn(true);
        when(reminderRepository.findById(21L)).thenReturn(Optional.of(reminder));
        when(noteRepository.findByIdAndUser_IdForUpdate(12L, 8L))
                .thenReturn(Optional.of(reminder.getNote()));
        when(reminderRepository.lockById(21L)).thenReturn(Optional.of(reminder));
        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        deliveryService.processReminder(21L);

        assertEquals(ReminderStatus.PENDING, reminder.getStatus());
        assertNotNull(reminder.getNextAttemptAt());
        assertEquals("MailSendException", reminder.getLastFailure());
        verify(reminderRepository).save(reminder);
    }

    @Test
    void doesNotSendWhenReminderCannotBeClaimedAgain() {

        when(claimService.claimReminder(21L)).thenReturn(false);

        deliveryService.processReminder(21L);

        verifyNoInteractions(mailSender);
        verify(reminderRepository, never()).findById(anyLong());
    }

    @Test
    void cancelsInsteadOfSendingWhenNoteWasTrashed() {

        reminder.getNote().setTrashed(true);
        when(claimService.claimReminder(21L)).thenReturn(true);
        when(reminderRepository.findById(21L)).thenReturn(Optional.of(reminder));
        when(noteRepository.findByIdAndUser_IdForUpdate(12L, 8L))
                .thenReturn(Optional.of(reminder.getNote()));
        when(reminderRepository.lockById(21L)).thenReturn(Optional.of(reminder));

        deliveryService.processReminder(21L);

        assertEquals(ReminderStatus.CANCELLED, reminder.getStatus());
        verifyNoInteractions(mailSender);
    }

    @Test
    void cancelsInsteadOfSendingWhenReminderOwnerNoLongerOwnsNote() {

        User otherUser = new User();
        otherUser.setId(99L);
        reminder.getNote().setUser(otherUser);
        when(claimService.claimReminder(21L)).thenReturn(true);
        when(reminderRepository.findById(21L)).thenReturn(Optional.of(reminder));
        when(noteRepository.findByIdAndUser_IdForUpdate(12L, 8L))
                .thenReturn(Optional.of(reminder.getNote()));
        when(reminderRepository.lockById(21L)).thenReturn(Optional.of(reminder));

        deliveryService.processReminder(21L);

        assertEquals(ReminderStatus.CANCELLED, reminder.getStatus());
        verifyNoInteractions(mailSender);
    }

    @Test
    void marksReminderFailedAfterFinalMailAttempt() {

        reminder.setRetryCount(ReminderClaimService.MAX_ATTEMPTS);
        when(claimService.claimReminder(21L)).thenReturn(true);
        when(reminderRepository.findById(21L)).thenReturn(Optional.of(reminder));
        when(noteRepository.findByIdAndUser_IdForUpdate(12L, 8L))
                .thenReturn(Optional.of(reminder.getNote()));
        when(reminderRepository.lockById(21L)).thenReturn(Optional.of(reminder));
        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        deliveryService.processReminder(21L);

        assertEquals(ReminderStatus.FAILED, reminder.getStatus());
        assertEquals("MailSendException", reminder.getLastFailure());
        verify(reminderRepository).save(reminder);
    }
}
