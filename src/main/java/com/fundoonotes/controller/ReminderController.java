
package com.fundoonotes.controller;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Reminder Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Exposes the REST endpoint for requesting note reminders.
 * Delegates validation and event publishing to ReminderService.
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/{noteId}/reminders")
    public ResponseEntity<String> requestReminder(
            @PathVariable Long noteId,
            @Valid @RequestBody ReminderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        reminderService.requestReminder(
                noteId,
                request,
                userDetails.getUsername()
        );

        return ResponseEntity.accepted()
                .body("Reminder request accepted for processing");
    }
}
