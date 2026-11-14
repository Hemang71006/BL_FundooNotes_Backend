
package com.fundoonotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Response DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Represents note information returned by the API.
 * Provides note details and status without exposing the User entity.
 */

@Getter
@AllArgsConstructor
public class NoteResponse {

    private Long id;
    private String title;
    private String description;
    private boolean pinned;
    private boolean archived;
    private boolean trashed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
