package com.fundoonotes.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Processor
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Coordinates atomic reminder claims and transactional email delivery.
 * Invokes the delivery bean across a Spring proxy boundary.
 */
@Service
@RequiredArgsConstructor
public class ReminderProcessor {

    private final ReminderClaimService reminderClaimService;
    private final ReminderDeliveryService reminderDeliveryService;

    public void processReminder(Long reminderId) {

        reminderClaimService.claimReminder(reminderId)
                .ifPresent(claimToken ->
                        reminderDeliveryService.deliverClaimedReminder(reminderId, claimToken));
    }
}
