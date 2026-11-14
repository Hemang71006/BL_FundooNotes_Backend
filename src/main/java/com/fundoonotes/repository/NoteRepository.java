
package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Repository
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Provides database operations for the Note entity.
 * Retrieves notes according to their owner and status.
 */

public interface NoteRepository extends JpaRepository<Note, Long> {

    // Finds all active notes belonging to a user.
    List<Note> findByUser_IdAndArchivedFalseAndTrashedFalseOrderByUpdatedAtDesc(Long userId);

    // Finds archived notes belonging to a user.
    List<Note> findByUser_IdAndArchivedTrueAndTrashedFalseOrderByUpdatedAtDesc(Long userId);

    // Finds trashed notes belonging to a user.
    List<Note> findByUser_IdAndTrashedTrueOrderByUpdatedAtDesc(Long userId);

    // Finds a note only when it belongs to the specified user.
    Optional<Note> findByIdAndUser_Id(Long id, Long userId);
}
