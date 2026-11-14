package com.fundoonotes.service.impl;

import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.NoteSearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Note Search - Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Implements keyword-based note searching and label filtering.
 * Restricts search results to the authenticated user's notes.
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteSearchServiceImpl implements NoteSearchService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    @Override
    public List<NoteResponse> searchNotes(
            String keyword, Long labelId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        if (labelId != null &&
                !labelRepository.existsByIdAndUser_Id(labelId, user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Label not found");
        }

        String searchKeyword = keyword == null
                ? null
                : keyword.trim();

        return noteRepository.searchActiveNotes(
                        user.getId(), searchKeyword, labelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NoteResponse toResponse(Note note) {

        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getDescription(),
                note.isPinned(),
                note.isArchived(),
                note.isTrashed(),
                note.getCreatedAt(),
                note.getUpdatedAt());
    }
}