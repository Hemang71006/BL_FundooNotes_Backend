package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.entity.User;
import com.fundoonotes.service.impl.ReminderClaimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Repository Tests
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Verifies reminder persistence and owner-scoped repository queries.
 * Uses the configured MySQL database instead of an embedded substitute.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReminderRepositoryTest {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Note note;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setFirstName("Reminder");
        user.setLastName("Test");
        user.setEmail("reminder-repository-test@example.com");
        user.setPassword("encoded-test-password");
        user = userRepository.save(user);

        note = new Note();
        note.setTitle("Persisted reminder note");
        note.setUser(user);
        note = noteRepository.save(note);
    }

    @Test
    void persistsReminderAndQueriesItForItsOwner() {

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(LocalDateTime.now().plusHours(1));
        reminder.setStatus(ReminderStatus.PENDING);

        Reminder savedReminder = reminderRepository.saveAndFlush(reminder);

        assertNotNull(savedReminder.getId());
        assertNotNull(savedReminder.getCreatedAt());
        assertNotNull(savedReminder.getUpdatedAt());

        List<Reminder> reminders =
                reminderRepository.findByUser_IdOrderByReminderAtAscCreatedAtAsc(
                        user.getId()
                );

        assertEquals(1, reminders.size());
        assertEquals(note.getId(), reminders.get(0).getNote().getId());
        assertEquals(ReminderStatus.PENDING, reminders.get(0).getStatus());
    }

    @Test
    void findsOnlyDuePendingRemindersForRecovery() {

        Reminder dueReminder = new Reminder();
        dueReminder.setNote(note);
        dueReminder.setUser(user);
        dueReminder.setReminderAt(LocalDateTime.now().minusMinutes(1));
        reminderRepository.saveAndFlush(dueReminder);

        List<Long> dueIds = reminderRepository.findDueReminderIds(
                LocalDateTime.now(),
                LocalDateTime.now().minusMinutes(
                        ReminderClaimService.PROCESSING_LEASE_MINUTES
                ),
                ReminderClaimService.MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING,
                org.springframework.data.domain.PageRequest.of(0, 10)
        );

        assertEquals(1, dueIds.size());
    }

    @Test
    void atomicallyClaimsDueReminderOnlyOnce() {

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(LocalDateTime.now().minusMinutes(1));
        reminder = reminderRepository.saveAndFlush(reminder);

        LocalDateTime now = LocalDateTime.now();
        int firstClaim = reminderRepository.claimDueReminder(
                reminder.getId(),
                now,
                now.minusMinutes(ReminderClaimService.PROCESSING_LEASE_MINUTES),
                ReminderClaimService.MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING
        );
        int duplicateClaim = reminderRepository.claimDueReminder(
                reminder.getId(),
                now,
                now.minusMinutes(ReminderClaimService.PROCESSING_LEASE_MINUTES),
                ReminderClaimService.MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING
        );

        assertEquals(1, firstClaim);
        assertEquals(0, duplicateClaim);
        assertEquals(
                ReminderStatus.PROCESSING,
                reminderRepository.findById(reminder.getId()).orElseThrow().getStatus()
        );
    }

    @Test
    void cancelsActiveRemindersForNoteLifecycleChanges() {

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(LocalDateTime.now().plusHours(1));
        reminderRepository.saveAndFlush(reminder);

        int cancelledCount = reminderRepository.cancelActiveByNoteAndUser(
                note.getId(),
                user.getId(),
                List.of(
                        ReminderStatus.PENDING,
                        ReminderStatus.PROCESSING,
                        ReminderStatus.FAILED
                ),
                ReminderStatus.CANCELLED,
                LocalDateTime.now()
        );

        assertEquals(1, cancelledCount);
        assertEquals(
                ReminderStatus.CANCELLED,
                reminderRepository.findById(reminder.getId()).orElseThrow().getStatus()
        );
    }

    @Test
    void marksExpiredProcessingRecordFailedAfterRetryLimit() {

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(LocalDateTime.now().minusHours(1));
        reminder.setStatus(ReminderStatus.PROCESSING);
        reminder.setRetryCount(ReminderClaimService.MAX_ATTEMPTS);
        reminder.setProcessingStartedAt(LocalDateTime.now().minusMinutes(20));
        reminder = reminderRepository.saveAndFlush(reminder);

        LocalDateTime now = LocalDateTime.now();
        int failedCount = reminderRepository.failExhaustedReminders(
                now,
                now.minusMinutes(ReminderClaimService.PROCESSING_LEASE_MINUTES),
                ReminderClaimService.MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING,
                ReminderStatus.FAILED,
                "RETRIES_EXHAUSTED"
        );

        assertEquals(1, failedCount);
        assertEquals(
                ReminderStatus.FAILED,
                reminderRepository.findById(reminder.getId()).orElseThrow().getStatus()
        );
    }
}
