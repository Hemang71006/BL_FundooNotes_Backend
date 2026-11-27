
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.NoteService;
import com.fundoonotes.service.ReminderService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Implements note management business operations.
 * Ensures users can access and modify only their own notes.
 * Uses Redis caching to improve note retrieval performance.
 */

@Service
@RequiredArgsConstructor
@Transactional
public class NoteServiceImpl implements NoteService {

    private static final Logger logger =
            LoggerFactory.getLogger(NoteServiceImpl.class);

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ReminderService reminderService;

    // Creates a new note and clears cached note data after successful creation.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public NoteResponse createNote(NoteRequest request, String email) {

        logger.info("Creating a new note");

        User user = findUserByEmail(email);

        Note note = new Note();
        note.setTitle(request.getTitle().trim());
        note.setDescription(request.getDescription());
        note.setUser(user);

        Note savedNote = noteRepository.save(note);

        return mapToResponse(savedNote);
    }

    // Caches active notes separately for each authenticated user.
    @Cacheable(cacheNames = "activeNotes", key = "#p0")
    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes(String email) {

        logger.debug("Retrieving active notes");

        User user = findUserByEmail(email);

        return noteRepository
                .findByUser_IdAndArchivedFalseAndTrashedFalseOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Caches archived notes separately for each authenticated user.
    @Cacheable(cacheNames = "archivedNotes", key = "#p0")
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

    // Caches trashed notes separately for each authenticated user.
    @Cacheable(cacheNames = "trashedNotes", key = "#p0")
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

    // Caches an individual note using its ID and owner's email.
    @Cacheable(cacheNames = "noteById", key = "#p0 + ':' + #p1")
    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long noteId, String email) {

        User user = findUserByEmail(email);
        Note note = findNoteByIdAndUser(noteId, user.getId());

        return mapToResponse(note);
    }

    // Updates a note and clears all cached note data.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public NoteResponse updateNote(Long noteId, NoteRequest request, String email) {

        logger.info("Updating note with id={}", noteId);

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

    // Permanently deletes a note and clears cached note data.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public void deleteNote(Long noteId, String email) {

        logger.info("Permanently deleting note with id={}", noteId);

        User user = findUserByEmail(email);
        Note note = noteRepository.findByIdAndUser_IdForUpdate(noteId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));

        reminderService.deleteForNote(note.getId(), user.getId());
        noteRepository.delete(note);
    }

    // Pins a note and clears cached note data.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public NoteResponse pinNote(Long noteId, String email) {

        logger.info("Pinning note with id={}", noteId);

        Note note = getNoteForStatusChange(noteId, email);
        note.setPinned(true);

        return mapToResponse(noteRepository.save(note));
    }

    // Unpins a note and clears cached note data.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public NoteResponse unpinNote(Long noteId, String email) {

        Note note = getNoteForStatusChange(noteId, email);
        note.setPinned(false);

        return mapToResponse(noteRepository.save(note));
    }

    // Archives a note and clears cached note data.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public NoteResponse archiveNote(Long noteId, String email) {

        logger.info("Archiving note with id={}", noteId);

        Note note = getNoteForStatusChange(noteId, email);
        note.setArchived(true);

        return mapToResponse(noteRepository.save(note));
    }

    // Unarchives a note and clears cached note data.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public NoteResponse unarchiveNote(Long noteId, String email) {

        Note note = getNoteForStatusChange(noteId, email);
        note.setArchived(false);

        return mapToResponse(noteRepository.save(note));
    }

    // Moves a note to trash and clears cached note data.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public NoteResponse trashNote(Long noteId, String email) {

        logger.info("Moving note to trash with id={}", noteId);

        Note note = findNoteForUserForUpdate(noteId, email);
        note.setTrashed(true);
        reminderService.cancelActiveForNote(note.getId(), note.getUser().getId());

        return mapToResponse(noteRepository.save(note));
    }

    // Restores a note from trash and clears cached note data.
    @CacheEvict(
            cacheNames = {"activeNotes", "archivedNotes", "trashedNotes", "noteById"},
            allEntries = true
    )
    @Override
    public NoteResponse restoreNote(Long noteId, String email) {

        logger.info("Restoring note with id={}", noteId);

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

    private Note findNoteForUserForUpdate(Long noteId, String email) {

        User user = findUserByEmail(email);

        return noteRepository.findByIdAndUser_IdForUpdate(noteId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));
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
