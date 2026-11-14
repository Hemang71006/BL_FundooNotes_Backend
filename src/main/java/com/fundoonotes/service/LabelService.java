package com.fundoonotes.service;

import com.fundoonotes.dto.request.LabelRequest;
import com.fundoonotes.dto.response.LabelResponse;
import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Label Management - Service
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines operations for label management.
 * Includes label CRUD and note-label association operations.
 */

public interface LabelService {

    LabelResponse createLabel(LabelRequest request, String email);

    List<LabelResponse> getAllLabels(String email);

    LabelResponse updateLabel(Long labelId, LabelRequest request, String email);

    void deleteLabel(Long labelId, String email);

    LabelResponse addLabelToNote(Long noteId, Long labelId, String email);

    void removeLabelFromNote(Long noteId, Long labelId, String email);
}