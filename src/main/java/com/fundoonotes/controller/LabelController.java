package com.fundoonotes.controller;

import com.fundoonotes.dto.request.LabelRequest;
import com.fundoonotes.dto.response.LabelResponse;
import com.fundoonotes.service.LabelService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Label Management - Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Exposes REST APIs for label management.
 * Uses the authenticated user's email to identify label ownership.
 */

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelResponse createLabel(
            @Valid @RequestBody LabelRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return labelService.createLabel(
                request, userDetails.getUsername());
    }

    @GetMapping
    public List<LabelResponse> getAllLabels(
            @AuthenticationPrincipal UserDetails userDetails) {

        return labelService.getAllLabels(userDetails.getUsername());
    }

    @PutMapping("/{labelId}")
    public LabelResponse updateLabel(
            @PathVariable Long labelId,
            @Valid @RequestBody LabelRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return labelService.updateLabel(
                labelId, request, userDetails.getUsername());
    }

    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLabel(
            @PathVariable Long labelId,
            @AuthenticationPrincipal UserDetails userDetails) {

        labelService.deleteLabel(labelId, userDetails.getUsername());
    }
}