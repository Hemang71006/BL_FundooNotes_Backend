
package com.fundoonotes.service;

import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;

import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Service Interface
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines the business operations available for note management.
 * Provides methods for note CRUD and status management.
 */

public interface NoteService {

    NoteResponse createNote(NoteRequest request, String email);

    List<NoteResponse> getAllNotes(String email);

    List<NoteResponse> getArchivedNotes(String email);

    List<NoteResponse> getTrashedNotes(String email);

    NoteResponse getNoteById(Long noteId, String email);

    NoteResponse updateNote(Long noteId, NoteRequest request, String email);

    void deleteNote(Long noteId, String email);

    NoteResponse pinNote(Long noteId, String email);

    NoteResponse unpinNote(Long noteId, String email);

    NoteResponse archiveNote(Long noteId, String email);

    NoteResponse unarchiveNote(Long noteId, String email);

    NoteResponse trashNote(Long noteId, String email);

    NoteResponse restoreNote(Long noteId, String email);
}
