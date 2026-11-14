
package com.fundoonotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Request DTO
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Holds the information required to create or update a note.
 * Validates the title and description received from the client.
 */

@Getter
@Setter
@NoArgsConstructor
public class NoteRequest {

    @NotBlank(message = "Note title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
}
