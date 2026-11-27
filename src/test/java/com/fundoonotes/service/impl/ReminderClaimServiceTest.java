package com.fundoonotes.service.impl;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import com.fundoonotes.util.ReminderTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Claim Tests
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Verifies atomic reminder claims and note lifecycle checks.
 * Ensures trashed notes cannot enter the delivery flow.
 */
@ExtendWith(MockitoExtension.class)
class ReminderClaimServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private NoteRepository noteRepository;

    private ReminderClaimService service;
    private Reminder reminder;
    private Note note;

    @BeforeEach
    void setUp() {

        service = new ReminderClaimService(reminderRepository, noteRepository);

        User user = new User();
        user.setId(8L);

        note = new Note();
        note.setId(12L);
        note.setUser(user);

        reminder = new Reminder();
        reminder.setId(21L);
        reminder.setUser(user);
        reminder.setNote(note);
    }

    @Test
    void claimsReminderOnlyAfterLockingItsOwnedActiveNote() {

        when(reminderRepository.findById(21L))
                .thenReturn(Optional.of(reminder));
        when(noteRepository.findByIdAndUser_IdForUpdate(12L, 8L))
                .thenReturn(Optional.of(note));
        when(reminderRepository.claimDueReminder(
                eq(21L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(ReminderClaimService.MAX_ATTEMPTS),
                any(),
                any(),
                anyString()
        )).thenReturn(1);

        Optional<String> claimToken = service.claimReminder(21L);
        assertTrue(claimToken.isPresent());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(reminderRepository).claimDueReminder(
                eq(21L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(ReminderClaimService.MAX_ATTEMPTS),
                any(),
                any(),
                tokenCaptor.capture()
        );
        assertEquals(claimToken.orElseThrow(), tokenCaptor.getValue());
    }

    @Test
    void cancelsInsteadOfClaimingReminderForTrashedNote() {

        note.setTrashed(true);
        when(reminderRepository.findById(21L))
                .thenReturn(Optional.of(reminder));
        when(noteRepository.findByIdAndUser_IdForUpdate(12L, 8L))
                .thenReturn(Optional.of(note));
        when(reminderRepository.cancelActiveByNoteAndUser(
                eq(12L),
                eq(8L),
                anyList(),
                any(),
                any(LocalDateTime.class)
        )).thenReturn(1);

        assertFalse(service.claimReminder(21L).isPresent());

        verify(reminderRepository, never()).claimDueReminder(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyInt(),
                any(),
                any(),
                anyString()
        );
    }
}
