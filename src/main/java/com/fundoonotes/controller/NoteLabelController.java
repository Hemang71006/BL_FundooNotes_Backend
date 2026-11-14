
package com.fundoonotes.controller;

import com.fundoonotes.dto.response.LabelResponse;
import com.fundoonotes.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Note Label Management - Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Exposes APIs to associate labels with notes and remove those associations.
 * Uses the authenticated user's email to verify ownership.
 */

@RestController
@RequestMapping("/api/notes/{noteId}/labels")
@RequiredArgsConstructor
public class NoteLabelController {

    private final LabelService labelService;

    @PostMapping("/{labelId}")
    @ResponseStatus(HttpStatus.OK)
    public LabelResponse addLabelToNote(
            @PathVariable Long noteId,
            @PathVariable Long labelId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return labelService.addLabelToNote(
                noteId, labelId, userDetails.getUsername());
    }

    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLabelFromNote(
            @PathVariable Long noteId,
            @PathVariable Long labelId,
            @AuthenticationPrincipal UserDetails userDetails) {

        labelService.removeLabelFromNote(
                noteId, labelId, userDetails.getUsername());
    }
}
