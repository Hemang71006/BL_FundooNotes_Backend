
package com.fundoonotes.controller;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.service.NoteService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Exposes REST APIs for managing user notes.
 * Delegates note operations to the NoteService layer.
 */

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // Creates a new note.
    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            @Valid @RequestBody NoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        NoteResponse response = noteService.createNote(
                request,
                userDetails.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Retrieves all active notes of the authenticated user.
    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.getAllNotes(userDetails.getUsername())
        );
    }

    // Retrieves archived notes.
    @GetMapping("/archived")
    public ResponseEntity<List<NoteResponse>> getArchivedNotes(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.getArchivedNotes(userDetails.getUsername())
        );
    }

    // Retrieves trashed notes.
    @GetMapping("/trash")
    public ResponseEntity<List<NoteResponse>> getTrashedNotes(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.getTrashedNotes(userDetails.getUsername())
        );
    }

    // Retrieves a particular note.
    @GetMapping("/{noteId}")
    public ResponseEntity<NoteResponse> getNoteById(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.getNoteById(noteId, userDetails.getUsername())
        );
    }

    // Updates a note.
    @PutMapping("/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody NoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.updateNote(
                        noteId,
                        request,
                        userDetails.getUsername()
                )
        );
    }

    // Permanently deletes a note.
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        noteService.deleteNote(noteId, userDetails.getUsername());

        return ResponseEntity.noContent().build();
    }

    // Pins a note.
    @PatchMapping("/{noteId}/pin")
    public ResponseEntity<NoteResponse> pinNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.pinNote(noteId, userDetails.getUsername())
        );
    }

    // Unpins a note.
    @PatchMapping("/{noteId}/unpin")
    public ResponseEntity<NoteResponse> unpinNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.unpinNote(noteId, userDetails.getUsername())
        );
    }

    // Archives a note.
    @PatchMapping("/{noteId}/archive")
    public ResponseEntity<NoteResponse> archiveNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.archiveNote(noteId, userDetails.getUsername())
        );
    }

    // Unarchives a note.
    @PatchMapping("/{noteId}/unarchive")
    public ResponseEntity<NoteResponse> unarchiveNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.unarchiveNote(noteId, userDetails.getUsername())
        );
    }

    // Moves a note to trash.
    @PatchMapping("/{noteId}/trash")
    public ResponseEntity<NoteResponse> trashNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.trashNote(noteId, userDetails.getUsername())
        );
    }

    // Restores a note from trash.
    @PatchMapping("/{noteId}/restore")
    public ResponseEntity<NoteResponse> restoreNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                noteService.restoreNote(noteId, userDetails.getUsername())
        );
    }
}
