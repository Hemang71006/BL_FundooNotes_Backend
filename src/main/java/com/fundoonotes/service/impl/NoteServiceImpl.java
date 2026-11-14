
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.NoteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Implements note management business operations.
 * Ensures users can access and modify only their own notes.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    // Creates a new note for the authenticated user.
    @Override
    public NoteResponse createNote(NoteRequest request, String email) {

        User user = findUserByEmail(email);

        Note note = new Note();
        note.setTitle(request.getTitle().trim());
        note.setDescription(request.getDescription());
        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        return mapToResponse(savedNote);
    }

    // Retrieves active notes belonging to the authenticated user.
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes(String email) {

        User user = findUserByEmail(email);

        return noteRepository
                .findByUser_IdAndArchivedFalseAndTrashedFalseOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieves archived notes belonging to the authenticated user.
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getArchivedNotes(String email) {

        User user = findUserByEmail(email);

        return noteRepository
                .findByUser_IdAndArchivedTrueAndTrashedFalseOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieves trashed notes belonging to the authenticated user.
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getTrashedNotes(String email) {

        User user = findUserByEmail(email);

        return noteRepository
                .findByUser_IdAndTrashedTrueOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieves a note only if it belongs to the authenticated user.
    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long noteId, String email) {

        User user = findUserByEmail(email);
        Note note = findNoteByIdAndUser(noteId, user.getId());

        return mapToResponse(note);
    }

    // Updates the title and description of an existing note.
    @Override
    public NoteResponse updateNote(Long noteId, NoteRequest request, String email) {

        User user = findUserByEmail(email);
        Note note = findNoteByIdAndUser(noteId, user.getId());

        if (note.isTrashed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A trashed note cannot be updated"
            );
        }

        note.setTitle(request.getTitle().trim());
        note.setDescription(request.getDescription());

        Note updatedNote = noteRepository.save(note);

        return mapToResponse(updatedNote);
    }

    // Permanently deletes a note owned by the authenticated user.
    @Override
    public void deleteNote(Long noteId, String email) {

        User user = findUserByEmail(email);
        Note note = findNoteByIdAndUser(noteId, user.getId());

        noteRepository.delete(note);
    }

    // Pins a note.
    @Override
    public NoteResponse pinNote(Long noteId, String email) {

        Note note = getNoteForStatusChange(noteId, email);
        note.setPinned(true);

        return mapToResponse(noteRepository.save(note));
    }

    // Unpins a note.
    @Override
    public NoteResponse unpinNote(Long noteId, String email) {

        Note note = getNoteForStatusChange(noteId, email);
        note.setPinned(false);

        return mapToResponse(noteRepository.save(note));
    }

    // Archives a note.
    @Override
    public NoteResponse archiveNote(Long noteId, String email) {

        Note note = getNoteForStatusChange(noteId, email);
        note.setArchived(true);

        return mapToResponse(noteRepository.save(note));
    }

    // Removes a note from the archive.
    @Override
    public NoteResponse unarchiveNote(Long noteId, String email) {

        Note note = getNoteForStatusChange(noteId, email);
        note.setArchived(false);

        return mapToResponse(noteRepository.save(note));
    }

    // Moves a note into the trash without permanently deleting it.
    @Override
    public NoteResponse trashNote(Long noteId, String email) {

        Note note = findNoteForUser(noteId, email);
        note.setTrashed(true);

        return mapToResponse(noteRepository.save(note));
    }

    // Restores a note from the trash.
    @Override
    public NoteResponse restoreNote(Long noteId, String email) {

        Note note = findNoteForUser(noteId, email);
        note.setTrashed(false);

        return mapToResponse(noteRepository.save(note));
    }

    // Finds a user using the email from the authenticated JWT principal.
    private User findUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }

    // Finds a note only when it belongs to the specified user.
    private Note findNoteByIdAndUser(Long noteId, Long userId) {

        return noteRepository.findByIdAndUser_Id(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));
    }

    // Retrieves a note for status changes, excluding trashed notes.
    private Note getNoteForStatusChange(Long noteId, String email) {

        Note note = findNoteForUser(noteId, email);

        if (note.isTrashed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A trashed note cannot have its status changed"
            );
        }

        return note;
    }

    // Finds a note belonging to the authenticated user.
    private Note findNoteForUser(Long noteId, String email) {

        User user = findUserByEmail(email);

        return findNoteByIdAndUser(noteId, user.getId());
    }

    // Converts a Note entity into a safe API response DTO.
    private NoteResponse mapToResponse(Note note) {

        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getDescription(),
                note.isPinned(),
                note.isArchived(),
                note.isTrashed(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}