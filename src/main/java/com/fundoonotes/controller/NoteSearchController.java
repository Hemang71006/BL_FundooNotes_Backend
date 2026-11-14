
package com.fundoonotes.controller;

import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.service.NoteSearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Note Search - Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Exposes REST APIs for searching and filtering notes.
 * Supports optional keyword, label ID, and status parameters.
 */

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteSearchController {

    private final NoteSearchService noteSearchService;

    @GetMapping("/search")
    public List<NoteResponse> searchNotes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long labelId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {

        return noteSearchService.searchNotes(
                keyword,
                labelId,
                status,
                userDetails.getUsername());
    }
}
