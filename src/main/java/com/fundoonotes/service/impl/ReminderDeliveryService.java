package com.fundoonotes.service.impl;

import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Delivery Service
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Revalidates reminder ownership and note state before email delivery.
 * Records success or bounded retry state only after the SMTP operation.
 */
@Service
@RequiredArgsConstructor
public class ReminderDeliveryService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderDeliveryService.class);
    private static final DateTimeFormatter REMINDER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MAX_ATTEMPTS = ReminderClaimService.MAX_ATTEMPTS;
    private static final long RETRY_BASE_SECONDS = 30;

    private final ReminderClaimService reminderClaimService;
    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void processReminder(Long reminderId) {

        if (reminderClaimService.claimReminder(reminderId)) {
            deliverClaimedReminder(reminderId);
        }
    }

    @Transactional
    public void deliverClaimedReminder(Long reminderId) {

        Optional<Reminder> candidateResult = reminderRepository.findById(reminderId);
        if (candidateResult.isEmpty()) {
            return;
        }

        Reminder candidate = candidateResult.get();
        Optional<Note> noteResult = noteRepository.findByIdAndUser_IdForUpdate(
                candidate.getNote().getId(),
                candidate.getUser().getId()
        );
        Optional<Reminder> reminderResult = reminderRepository.lockById(reminderId);

        if (reminderResult.isEmpty()) {
            return;
        }

        Reminder reminder = reminderResult.get();
        if (reminder.getStatus() != ReminderStatus.PROCESSING) {
            return;
        }

        if (noteResult.isEmpty()
                || noteResult.get().isTrashed()
                || !noteResult.get().getUser().getId()
                .equals(reminder.getUser().getId())) {
            reminder.setStatus(ReminderStatus.CANCELLED);
            reminder.setProcessingStartedAt(null);
            reminder.setNextAttemptAt(null);
            reminderRepository.save(reminder);
            return;
        }

        Note note = noteResult.get();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(reminder.getUser().getEmail());
        message.setSubject("Fundoo Notes - Note Reminder");
        message.setText(
                "Hello,\n\n" +
                        "This is your scheduled Fundoo Notes reminder.\n\n" +
                        "Note: " + note.getTitle() + "\n" +
                        "Scheduled for: " + reminder.getReminderAt()
                        .format(REMINDER_TIME_FORMAT) + "\n\n" +
                        "Please open Fundoo Notes to view your note.\n\n" +
                        "Regards,\nFundoo Notes Team"
        );

        try {
            mailSender.send(message);
            LocalDateTime deliveredAt = LocalDateTime.now();
            reminder.setStatus(ReminderStatus.SENT);
            reminder.setDeliveredAt(deliveredAt);
            reminder.setProcessingStartedAt(null);
            reminder.setNextAttemptAt(null);
            reminder.setLastFailure(null);
            reminder.setUpdatedAt(deliveredAt);
            reminderRepository.save(reminder);

            logger.info("Reminder email sent: reminderId={}", reminderId);
        } catch (MailException exception) {
            scheduleRetry(reminder, exception);
        }
    }

    private void scheduleRetry(Reminder reminder, MailException exception) {

        LocalDateTime now = LocalDateTime.now();
        reminder.setLastFailure(exception.getClass().getSimpleName());
        reminder.setProcessingStartedAt(null);
        reminder.setUpdatedAt(now);

        if (reminder.getRetryCount() >= MAX_ATTEMPTS) {
            reminder.setStatus(ReminderStatus.FAILED);
            reminder.setNextAttemptAt(null);
        } else {
            long retryDelay = RETRY_BASE_SECONDS
                    * (1L << Math.min(reminder.getRetryCount() - 1, 7));
            reminder.setStatus(ReminderStatus.PENDING);
            reminder.setNextAttemptAt(now.plusSeconds(retryDelay));
        }

        reminderRepository.save(reminder);
        logger.warn(
                "Reminder email failed: reminderId={}, attempt={}, errorType={}, status={}",
                reminder.getId(),
                reminder.getRetryCount(),
                exception.getClass().getSimpleName(),
                reminder.getStatus()
        );
    }
}
