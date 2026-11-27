
package com.fundoonotes.controller;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.dto.response.ReminderResponse;
import com.fundoonotes.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Controller
 * Author       : Hemang
 * Date         : 27-11-2026
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

        reminderService.createReminder(
                noteId,
                request,
                userDetails.getUsername()
        );

        return ResponseEntity.accepted()
                .body("Reminder request accepted for processing");
    }

    @GetMapping("/reminders")
    public ResponseEntity<List<ReminderResponse>> getReminders(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                reminderService.getReminders(userDetails.getUsername())
        );
    }

    @GetMapping("/reminders/upcoming")
    public ResponseEntity<List<ReminderResponse>> getUpcomingReminders(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                reminderService.getUpcomingReminders(userDetails.getUsername())
        );
    }

    @GetMapping("/{noteId}/reminders")
    public ResponseEntity<List<ReminderResponse>> getNoteReminders(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                reminderService.getNoteReminders(
                        noteId,
                        userDetails.getUsername()
                )
        );
    }

    @PutMapping("/reminders/{reminderId}")
    public ResponseEntity<ReminderResponse> updateReminder(
            @PathVariable Long reminderId,
            @Valid @RequestBody ReminderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                reminderService.updateReminder(
                        reminderId,
                        request,
                        userDetails.getUsername()
                )
        );
    }

    @PatchMapping("/reminders/{reminderId}/cancel")
    public ResponseEntity<ReminderResponse> cancelReminder(
            @PathVariable Long reminderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                reminderService.cancelReminder(
                        reminderId,
                        userDetails.getUsername()
                )
        );
    }

    @PatchMapping("/reminders/{reminderId}/complete")
    public ResponseEntity<ReminderResponse> completeReminder(
            @PathVariable Long reminderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                reminderService.completeReminder(
                        reminderId,
                        userDetails.getUsername()
                )
        );
    }

    @DeleteMapping("/reminders/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long reminderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        reminderService.deleteReminder(
                reminderId,
                userDetails.getUsername()
        );

        return ResponseEntity.noContent().build();
    }
}
