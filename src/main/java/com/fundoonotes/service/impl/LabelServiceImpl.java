package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.LabelRequest;
import com.fundoonotes.dto.response.LabelResponse;
import com.fundoonotes.entity.Label;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.LabelRepository;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.LabelService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Label Management - Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Implements label creation, retrieval, updating, and deletion.
 * Also manages the relationship between notes and labels.
 * Ensures users can access only their own labels and notes.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Override
    public LabelResponse createLabel(LabelRequest request, String email) {

        User user = getUser(email);
        String name = request.getName().trim();

        if (labelRepository.existsByUser_IdAndNameIgnoreCase(
                user.getId(), name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Label already exists");
        }

        Label label = new Label();
        label.setName(name);
        label.setColor(request.getColor());
        label.setUser(user);

        return toResponse(labelRepository.save(label));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelResponse> getAllLabels(String email) {

        User user = getUser(email);

        return labelRepository.findByUser_IdOrderByNameAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LabelResponse updateLabel(
            Long labelId, LabelRequest request, String email) {

        User user = getUser(email);
        Label label = getLabel(labelId, user.getId());
        String name = request.getName().trim();

        if (labelRepository.existsByUser_IdAndNameIgnoreCaseAndIdNot(
                user.getId(), name, labelId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Another label already uses this name");
        }

        label.setName(name);
        label.setColor(request.getColor());

        return toResponse(labelRepository.save(label));
    }

    @Override
    public void deleteLabel(Long labelId, String email) {

        User user = getUser(email);
        Label label = getLabel(labelId, user.getId());

        // Remove the label from associated notes before deleting it.
        List<Note> notes = noteRepository.findAllByUser_IdAndLabels_Id(
                user.getId(), labelId);

        for (Note note : notes) {
            note.getLabels().remove(label);
        }

        noteRepository.saveAll(notes);
        labelRepository.delete(label);
    }

    @Override
    public LabelResponse addLabelToNote(
            Long noteId, Long labelId, String email) {

        User user = getUser(email);

        Note note = getNote(noteId, user.getId());
        Label label = getLabel(labelId, user.getId());

        if (note.isTrashed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot add a label to a trashed note");
        }

        note.getLabels().add(label);
        noteRepository.save(note);

        return toResponse(label);
    }

    @Override
    public void removeLabelFromNote(
            Long noteId, Long labelId, String email) {

        User user = getUser(email);

        Note note = getNote(noteId, user.getId());
        Label label = getLabel(labelId, user.getId());

        note.getLabels().remove(label);
        noteRepository.save(note);
    }

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private Label getLabel(Long labelId, Long userId) {

        return labelRepository.findByIdAndUser_Id(labelId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Label not found"));
    }

    private Note getNote(Long noteId, Long userId) {

        return noteRepository.findByIdAndUser_Id(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Note not found"));
    }

    private LabelResponse toResponse(Label label) {

        return new LabelResponse(
                label.getId(),
                label.getName(),
                label.getColor());
    }
}