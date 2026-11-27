package com.fundoonotes.config;

import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.repository.ReminderRepository;
import com.fundoonotes.service.impl.ReminderClaimService;
import com.fundoonotes.service.impl.ReminderDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Recovery Scheduler
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Polls persisted reminders and recovers due work after restarts.
 * RabbitMQ wake-ups are an optimization, not the durable schedule.
 */
@Component
@RequiredArgsConstructor
public class ReminderRecoveryScheduler {

    private static final int BATCH_SIZE = 100;

    private final ReminderRepository reminderRepository;
    private final ReminderClaimService reminderClaimService;
    private final ReminderDeliveryService reminderDeliveryService;

    @Scheduled(fixedDelayString = "${app.reminder.poll-interval-ms:10000}")
    public void processDueReminders() {

        reminderClaimService.failExhaustedReminders();
        LocalDateTime now = LocalDateTime.now();
        List<Long> reminderIds = reminderRepository.findDueReminderIds(
                now,
                now.minusMinutes(ReminderClaimService.PROCESSING_LEASE_MINUTES),
                ReminderClaimService.MAX_ATTEMPTS,
                ReminderStatus.PENDING,
                ReminderStatus.PROCESSING,
                PageRequest.of(0, BATCH_SIZE)
        );

        for (Long reminderId : reminderIds) {
            reminderDeliveryService.processReminder(reminderId);
        }
    }
}
