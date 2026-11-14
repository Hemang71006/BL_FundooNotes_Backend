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
 * Supports ownership checks, note filtering, searching, and label associations.
 */

public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByIdAndUser_Id(Long id, Long userId);

    List<Note> findByUser_IdAndArchivedFalseAndTrashedFalseOrderByUpdatedAtDesc(
            Long userId);

    List<Note> findByUser_IdAndArchivedTrueAndTrashedFalseOrderByUpdatedAtDesc(
            Long userId);

    List<Note> findByUser_IdAndTrashedTrueOrderByUpdatedAtDesc(
            Long userId);

    List<Note> findAllByUser_IdAndLabels_Id(Long userId, Long labelId);

    @Query("""
        SELECT DISTINCT n
        FROM Note n
        LEFT JOIN n.labels l
        WHERE n.user.id = :userId
          AND n.archived = false
          AND n.trashed = false
          AND (
              :keyword IS NULL
              OR :keyword = ''
              OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(COALESCE(n.description, ''))
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (:labelId IS NULL OR l.id = :labelId)
        ORDER BY n.updatedAt DESC
        """)
    List<Note> searchActiveNotes(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("labelId") Long labelId);
}