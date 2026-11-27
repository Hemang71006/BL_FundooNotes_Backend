package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.dto.response.ReminderResponse;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.entity.ReminderStatus;
import com.fundoonotes.entity.User;
import com.fundoonotes.event.ReminderEvent;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Reminder Integration - Reminder Service Implementation
 * Author       : Hemang
 * Date         : 27-11-2026
 *
 * Description:
 * Persists reminders and enforces owner, date, and lifecycle rules.
 * Publishes reminder identifiers after commit for RabbitMQ wake-up processing.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReminderServiceImpl implements ReminderService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderServiceImpl.class);

    private static final List<ReminderStatus> ACTIVE_STATUSES = List.of(
            ReminderStatus.PENDING,
            ReminderStatus.PROCESSING,
            ReminderStatus.FAILED
    );

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ReminderResponse createReminder(
            Long noteId,
            ReminderRequest request,
            String email) {

        User user = findUserByEmail(email);
        Note note = noteRepository.findByIdAndUser_IdForUpdate(noteId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));
        validateFutureReminder(request);
        rejectTrashedNote(note);

        Reminder reminder = new Reminder();
        reminder.setNote(note);
        reminder.setUser(user);
        reminder.setReminderAt(request.getReminderAt());

        Reminder savedReminder = reminderRepository.saveAndFlush(reminder);
        eventPublisher.publishEvent(new ReminderEvent(savedReminder.getId()));

        logger.info(
                "Reminder persisted: reminderId={}, noteId={}, userId={}",
                savedReminder.getId(),
                note.getId(),
                user.getId()
        );

        return toResponse(savedReminder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getReminders(String email) {

        User user = findUserByEmail(email);
        return reminderRepository
                .findByUser_IdOrderByReminderAtAscCreatedAtAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getUpcomingReminders(String email) {

        User user = findUserByEmail(email);
        return reminderRepository
                .findByUser_IdAndStatusAndReminderAtGreaterThanEqualOrderByReminderAtAsc(
                        user.getId(),
                        ReminderStatus.PENDING,
                        LocalDateTime.now()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getNoteReminders(
            Long noteId,
            String email) {

        User user = findUserByEmail(email);
        findOwnedNote(noteId, user.getId());
        return reminderRepository
                .findByNote_IdAndUser_IdOrderByReminderAtAsc(noteId, user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ReminderResponse updateReminder(
            Long reminderId,
            ReminderRequest request,
            String email) {

        User user = findUserByEmail(email);
        validateFutureReminder(request);
        Reminder reminder = lockOwnedReminder(reminderId, user.getId());
        requireEditable(reminder);

        rejectTrashedNote(reminder.getNote());
        reminder.setReminderAt(request.getReminderAt());
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setRetryCount(0);
        reminder.setNextAttemptAt(null);
        reminder.setProcessingStartedAt(null);
        reminder.setLastFailure(null);
        reminder.setDeliveredAt(null);

        eventPublisher.publishEvent(new ReminderEvent(reminder.getId()));
        return toResponse(reminderRepository.save(reminder));
    }

    @Override
    public ReminderResponse cancelReminder(Long reminderId, String email) {

        User user = findUserByEmail(email);
        Reminder reminder = lockOwnedReminder(reminderId, user.getId());
        requireEditable(reminder);

        reminder.setStatus(ReminderStatus.CANCELLED);
        reminder.setNextAttemptAt(null);
        reminder.setProcessingStartedAt(null);
        return toResponse(reminderRepository.save(reminder));
    }

    @Override
    public ReminderResponse completeReminder(Long reminderId, String email) {

        User user = findUserByEmail(email);
        Reminder reminder = lockOwnedReminder(reminderId, user.getId());
        requireEditable(reminder);

        reminder.setStatus(ReminderStatus.COMPLETED);
        reminder.setNextAttemptAt(null);
        reminder.setProcessingStartedAt(null);
        return toResponse(reminderRepository.save(reminder));
    }

    @Override
    public void deleteReminder(Long reminderId, String email) {

        User user = findUserByEmail(email);
        Reminder reminder = lockOwnedReminder(reminderId, user.getId());
        reminderRepository.delete(reminder);
    }

    @Override
    public void cancelActiveForNote(Long noteId, Long userId) {

        reminderRepository.cancelActiveByNoteAndUser(
                noteId,
                userId,
                ACTIVE_STATUSES,
                ReminderStatus.CANCELLED,
                LocalDateTime.now()
        );
    }

    @Override
    public void deleteForNote(Long noteId, Long userId) {

        reminderRepository.deleteByNote_IdAndUser_Id(noteId, userId);
    }

    private User findUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));
    }

    private Note findOwnedNote(Long noteId, Long userId) {

        return noteRepository.findByIdAndUser_Id(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));
    }

    private Reminder lockOwnedReminder(Long reminderId, Long userId) {

        return reminderRepository.lockByIdAndUser_Id(reminderId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reminder not found"
                ));
    }

    private void validateFutureReminder(ReminderRequest request) {

        if (request == null
                || request.getReminderAt() == null
                || !request.getReminderAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reminder date and time must be in the future"
            );
        }
    }

    private void rejectTrashedNote(Note note) {

        if (note.isTrashed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A reminder cannot be requested for a trashed note"
            );
        }
    }

    private void requireEditable(Reminder reminder) {

        if (reminder.getStatus() != ReminderStatus.PENDING
                && reminder.getStatus() != ReminderStatus.FAILED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending or failed reminders can be changed"
            );
        }
    }

    private ReminderResponse toResponse(Reminder reminder) {

        return new ReminderResponse(
                reminder.getId(),
                reminder.getNote().getId(),
                reminder.getNote().getTitle(),
                reminder.getReminderAt(),
                reminder.getStatus(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt(),
                reminder.getDeliveredAt(),
                reminder.getRetryCount()
        );
    }
}
