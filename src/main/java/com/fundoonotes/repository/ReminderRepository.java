package com.fundoonotes.repository;

import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Repository
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Provides owner-scoped reminder retrieval and atomic delivery claims.
 * Supports durable polling and bounded recovery after restarts.
 */
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUser_IdOrderByReminderAtAscCreatedAtAsc(Long userId);

    List<Reminder> findByUser_IdAndStatusAndReminderAtGreaterThanEqualOrderByReminderAtAsc(
            Long userId,
            ReminderStatus status,
            LocalDateTime reminderAt
    );

    List<Reminder> findByNote_IdAndUser_IdOrderByReminderAtAsc(
            Long noteId,
            Long userId
    );

    Optional<Reminder> findByIdAndUser_Id(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reminder r WHERE r.id = :id")
    Optional<Reminder> lockById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Reminder r
            WHERE r.id = :id AND r.user.id = :userId
            """)
    Optional<Reminder> lockByIdAndUser_Id(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT r.id
            FROM Reminder r
            WHERE r.reminderAt <= :now
              AND r.retryCount < :maxAttempts
              AND (
                    (r.status = :pending
                        AND (r.nextAttemptAt IS NULL OR r.nextAttemptAt <= :now))
                    OR
                    (r.status = :processing
                        AND r.processingStartedAt <= :processingBefore)
              )
            ORDER BY r.reminderAt ASC
            """)
    List<Long> findDueReminderIds(
            @Param("now") LocalDateTime now,
            @Param("processingBefore") LocalDateTime processingBefore,
            @Param("maxAttempts") int maxAttempts,
            @Param("pending") ReminderStatus pending,
            @Param("processing") ReminderStatus processing,
            Pageable pageable
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Reminder r
            SET r.status = :processing,
                r.processingStartedAt = :now,
                r.claimToken = :claimToken,
                r.nextAttemptAt = NULL,
                r.retryCount = r.retryCount + 1,
                r.updatedAt = :now
            WHERE r.id = :id
              AND r.reminderAt <= :now
              AND r.retryCount < :maxAttempts
              AND (
                    (r.status = :pending
                        AND (r.nextAttemptAt IS NULL OR r.nextAttemptAt <= :now))
                    OR
                    (r.status = :processing
                        AND r.processingStartedAt <= :processingBefore)
              )
            """)
    int claimDueReminder(
            @Param("id") Long id,
            @Param("now") LocalDateTime now,
            @Param("processingBefore") LocalDateTime processingBefore,
            @Param("maxAttempts") int maxAttempts,
            @Param("pending") ReminderStatus pending,
            @Param("processing") ReminderStatus processing,
            @Param("claimToken") String claimToken
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Reminder r
            SET r.status = :failed,
                r.processingStartedAt = NULL,
                r.claimToken = NULL,
                r.lastFailure = :failure,
                r.updatedAt = :now
            WHERE r.retryCount >= :maxAttempts
              AND (
                    (r.status = :pending
                        AND r.reminderAt <= :now
                        AND (r.nextAttemptAt IS NULL OR r.nextAttemptAt <= :now))
                    OR
                    (r.status = :processing
                        AND r.processingStartedAt <= :processingBefore)
              )
            """)
    int failExhaustedReminders(
            @Param("now") LocalDateTime now,
            @Param("processingBefore") LocalDateTime processingBefore,
            @Param("maxAttempts") int maxAttempts,
            @Param("pending") ReminderStatus pending,
            @Param("processing") ReminderStatus processing,
            @Param("failed") ReminderStatus failed,
            @Param("failure") String failure
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Reminder r
            SET r.status = :cancelled,
                r.processingStartedAt = NULL,
                r.claimToken = NULL,
                r.nextAttemptAt = NULL,
                r.updatedAt = :now
            WHERE r.note.id = :noteId
              AND r.user.id = :userId
              AND r.status IN :activeStatuses
            """)
    int cancelActiveByNoteAndUser(
            @Param("noteId") Long noteId,
            @Param("userId") Long userId,
            @Param("activeStatuses") List<ReminderStatus> activeStatuses,
            @Param("cancelled") ReminderStatus cancelled,
            @Param("now") LocalDateTime now
    );

    int deleteByNote_IdAndUser_Id(Long noteId, Long userId);
}
