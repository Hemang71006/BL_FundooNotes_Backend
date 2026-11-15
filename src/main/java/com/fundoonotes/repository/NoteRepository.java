package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Notes Management - Repository
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Provides database operations for notes.
 * Supports ownership checks, filtering, searching,
 * and label associations.
 */
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Finds a note using its ID and owner ID.
     */
    Optional<Note> findByIdAndUser_Id(Long id, Long userId);

    /**
     * Finds all active notes belonging to a user.
     */
    List<Note> findByUser_IdAndArchivedFalseAndTrashedFalseOrderByUpdatedAtDesc(
            Long userId);

    /**
     * Finds all archived notes belonging to a user.
     */
    List<Note> findByUser_IdAndArchivedTrueAndTrashedFalseOrderByUpdatedAtDesc(
            Long userId);

    /**
     * Finds all trashed notes belonging to a user.
     */
    List<Note> findByUser_IdAndTrashedTrueOrderByUpdatedAtDesc(
            Long userId);

    /**
     * Finds notes associated with a particular label.
     */
    List<Note> findAllByUser_IdAndLabels_Id(Long userId, Long labelId);

    /**
     * Finds every note belonging to a specific user.
     * Used by the Excel export service.
     */
    List<Note> findAllByUser_Id(Long userId);

    /**
     * Searches notes based on keyword, label, and status.
     */
    @Query("""
        SELECT DISTINCT n
        FROM Note n
        LEFT JOIN n.labels l
        WHERE n.user.id = :userId
          AND (
              :keyword IS NULL
              OR :keyword = ''
              OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(COALESCE(n.description, ''))
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (:labelId IS NULL OR l.id = :labelId)
          AND (
              (:status = 'ACTIVE'
                  AND n.archived = false
                  AND n.trashed = false)
              OR
              (:status = 'PINNED'
                  AND n.pinned = true
                  AND n.archived = false
                  AND n.trashed = false)
              OR
              (:status = 'ARCHIVED'
                  AND n.archived = true
                  AND n.trashed = false)
              OR
              (:status = 'TRASHED'
                  AND n.trashed = true)
          )
        ORDER BY n.updatedAt DESC
        """)
    List<Note> searchNotesByStatus(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("labelId") Long labelId,
            @Param("status") String status);
}