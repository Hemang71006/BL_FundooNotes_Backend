package com.fundoonotes.service.impl;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import com.fundoonotes.util.ReminderTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<String> claimReminder(Long reminderId) {

        Optional<Reminder> candidateResult = reminderRepository.findById(reminderId);
        if (candidateResult.isEmpty()) {
            return Optional.empty();
        }

        Reminder candidate = candidateResult.get();
        Optional<Note> noteResult = noteRepository.findByIdAndUser_IdForUpdate(
                candidate.getNote().getId(),
                candidate.getUser().getId()
        );
        if (noteResult.isEmpty()) {
            return Optional.empty();
        }

        Note note = noteResult.get();
        LocalDateTime now = ReminderTime.now();
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
            return Optional.empty();
        }

        String claimToken = UUID.randomUUID().toString();
        int claimed = reminderRepository.claimDueReminder(
                reminderId,
                now,
                now.minusMinutes(PROCESSING_LEASE_MINUTES),
                MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING,
                claimToken
        );
        return claimed == 1 ? Optional.of(claimToken) : Optional.empty();
    }

    @Transactional
    public void failExhaustedReminders() {

        LocalDateTime now = ReminderTime.now();
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
