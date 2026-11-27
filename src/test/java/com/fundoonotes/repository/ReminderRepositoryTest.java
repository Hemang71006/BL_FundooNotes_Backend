package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.entity.User;
import com.fundoonotes.service.impl.ReminderClaimService;
import com.fundoonotes.util.ReminderTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
@Import(ReminderClaimService.class)
class ReminderRepositoryTest {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReminderClaimService reminderClaimService;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
        reminder.setReminderAt(ReminderTime.now().plusHours(1));
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
        dueReminder.setReminderAt(ReminderTime.now().minusMinutes(1));
        reminderRepository.saveAndFlush(dueReminder);

        List<Long> dueIds = reminderRepository.findDueReminderIds(
                ReminderTime.now(),
                ReminderTime.now().minusMinutes(
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
        reminder.setReminderAt(ReminderTime.now().minusMinutes(1));
        reminder = reminderRepository.saveAndFlush(reminder);

        LocalDateTime now = ReminderTime.now();
        int firstClaim = reminderRepository.claimDueReminder(
                reminder.getId(),
                now,
                now.minusMinutes(ReminderClaimService.PROCESSING_LEASE_MINUTES),
                ReminderClaimService.MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING,
                "first-claim-token"
        );
        int duplicateClaim = reminderRepository.claimDueReminder(
                reminder.getId(),
                now,
                now.minusMinutes(ReminderClaimService.PROCESSING_LEASE_MINUTES),
                ReminderClaimService.MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING,
                "second-claim-token"
        );

        assertEquals(1, firstClaim);
        assertEquals(0, duplicateClaim);
        assertEquals(
                ReminderStatus.PROCESSING,
                reminderRepository.findById(reminder.getId()).orElseThrow().getStatus()
        );
        assertEquals(
                "first-claim-token",
                reminderRepository.findById(reminder.getId()).orElseThrow().getClaimToken()
        );
    }

    @Test
    void concurrentlyClaimsDueReminderOnlyOnce() throws Exception {

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(ReminderTime.now().minusMinutes(1));
        reminder = reminderRepository.saveAndFlush(reminder);
        Long reminderId = reminder.getId();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> first = workers.submit(() -> tryClaim(reminderId, ready, start));
            Future<Boolean> second = workers.submit(() -> tryClaim(reminderId, ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            int claimCount = (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(1, claimCount);
            assertEquals(
                    ReminderStatus.PROCESSING,
                    reminderRepository.findById(reminderId).orElseThrow().getStatus()
            );
        } finally {
            start.countDown();
            workers.shutdownNow();
            workers.awaitTermination(10, TimeUnit.SECONDS);
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                reminderRepository.deleteById(reminderId);
                noteRepository.deleteById(note.getId());
                userRepository.deleteById(user.getId());
            });
        }
    }

    @Test
    void cancelsActiveRemindersForNoteLifecycleChanges() {

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(ReminderTime.now().plusHours(1));
        reminder.setStatus(ReminderStatus.PROCESSING);
        reminder.setProcessingStartedAt(ReminderTime.now());
        reminder.setClaimToken("active-claim-token");
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
                ReminderTime.now()
        );

        assertEquals(1, cancelledCount);
        assertEquals(
                ReminderStatus.CANCELLED,
                reminderRepository.findById(reminder.getId()).orElseThrow().getStatus()
        );
        assertNull(reminderRepository.findById(reminder.getId()).orElseThrow().getClaimToken());
    }

    @Test
    void marksExpiredProcessingRecordFailedAfterRetryLimit() {

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(ReminderTime.now().minusHours(1));
        reminder.setStatus(ReminderStatus.PROCESSING);
        reminder.setRetryCount(ReminderClaimService.MAX_ATTEMPTS);
        reminder.setProcessingStartedAt(ReminderTime.now().minusMinutes(20));
        reminder.setClaimToken("expired-token");
        reminder = reminderRepository.saveAndFlush(reminder);

        LocalDateTime now = ReminderTime.now();
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
        assertNull(reminderRepository.findById(reminder.getId()).orElseThrow().getClaimToken());
    }

    @Test
    void replacesFencingTokenWhenAnExpiredClaimIsReclaimed() {

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(ReminderTime.now().minusHours(1));
        reminder.setStatus(ReminderStatus.PROCESSING);
        reminder.setRetryCount(1);
        reminder.setProcessingStartedAt(ReminderTime.now().minusMinutes(20));
        reminder.setClaimToken("expired-token");
        reminder = reminderRepository.saveAndFlush(reminder);

        String replacementToken = "replacement-token";
        int claimed = reminderRepository.claimDueReminder(
                reminder.getId(),
                ReminderTime.now(),
                ReminderTime.now().minusMinutes(
                        ReminderClaimService.PROCESSING_LEASE_MINUTES
                ),
                ReminderClaimService.MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING,
                replacementToken
        );

        assertEquals(1, claimed);
        assertEquals(
                replacementToken,
                reminderRepository.findById(reminder.getId()).orElseThrow().getClaimToken()
        );
    }

    private boolean tryClaim(
            Long reminderId,
            CountDownLatch ready,
            CountDownLatch start) throws InterruptedException {

        ready.countDown();
        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out waiting to start concurrent claim");
        }
        return reminderClaimService.claimReminder(reminderId).isPresent();
    }
}
