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
 * Exposes the REST API for searching and filtering notes.
 * Accepts an optional keyword and an optional label ID.
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
            @AuthenticationPrincipal UserDetails userDetails) {

        return noteSearchService.searchNotes(
                keyword, labelId, userDetails.getUsername());
    }
}