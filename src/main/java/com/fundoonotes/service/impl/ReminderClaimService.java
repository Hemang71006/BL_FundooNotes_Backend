package com.fundoonotes.service.impl;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Claim Service
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Atomically claims due reminders with a bounded processing lease.
 * Locks the note first so trash and deletion cannot race a delivery claim.
 */
@Service
@RequiredArgsConstructor
public class ReminderClaimService {

    public static final int MAX_ATTEMPTS = 5;
    public static final int PROCESSING_LEASE_MINUTES = 15;

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderClaimService.class);
    private static final String RETRIES_EXHAUSTED = "RETRIES_EXHAUSTED";

    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public boolean claimReminder(Long reminderId) {

        Optional<Reminder> candidateResult = reminderRepository.findById(reminderId);
        if (candidateResult.isEmpty()) {
            return false;
        }

        Reminder candidate = candidateResult.get();
        Optional<Note> noteResult = noteRepository.findByIdAndUser_IdForUpdate(
                candidate.getNote().getId(),
                candidate.getUser().getId()
        );
        if (noteResult.isEmpty()) {
            return false;
        }

        Note note = noteResult.get();
        LocalDateTime now = LocalDateTime.now();
        if (note.isTrashed()) {
            reminderRepository.cancelActiveByNoteAndUser(
                    note.getId(),
                    candidate.getUser().getId(),
                    List.of(
                            ReminderStatus.PENDING,
                            ReminderStatus.PROCESSING,
                            ReminderStatus.FAILED
                    ),
                    ReminderStatus.CANCELLED,
                    now
            );
            return false;
        }

        return reminderRepository.claimDueReminder(
                reminderId,
                now,
                now.minusMinutes(PROCESSING_LEASE_MINUTES),
                MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING
        ) == 1;
    }

    @Transactional
    public void failExhaustedReminders() {

        LocalDateTime now = LocalDateTime.now();
        int failedCount = reminderRepository.failExhaustedReminders(
                now,
                now.minusMinutes(PROCESSING_LEASE_MINUTES),
                MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING,
                ReminderStatus.FAILED,
                RETRIES_EXHAUSTED
        );

        if (failedCount > 0) {
            logger.warn(
                    "Marked {} reminders failed after exhausting retries",
                    failedCount
            );
        }
    }
}
