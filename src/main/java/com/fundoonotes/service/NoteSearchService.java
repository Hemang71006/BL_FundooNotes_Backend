package com.fundoonotes.service;

import com.fundoonotes.dto.response.NoteResponse;
import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Note Search - Service
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines operations for searching and filtering active notes.
 * Supports keyword searches and label-based filtering.
 */

public interface NoteSearchService {

    List<NoteResponse> searchNotes(
            String keyword, Long labelId, String email);
}