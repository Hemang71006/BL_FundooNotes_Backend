
package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.event.ReminderEvent;
import com.fundoonotes.repository.NoteRepository;
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

/**
 * Project Name : Fundoo Notes Backend
 * Module       : RabbitMQ Messaging - Reminder Service Implementation
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Validates reminder requests and verifies note ownership.
 * Publishes an application event for asynchronous RabbitMQ processing.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReminderServiceImpl implements ReminderService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderServiceImpl.class);

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void requestReminder(
            Long noteId,
            ReminderRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        Note note = noteRepository.findByIdAndUser_Id(
                        noteId,
                        user.getId()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));

        if (note.isTrashed()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A reminder cannot be requested for a trashed note"
            );
        }

        if (request.getReminderAt() == null
                || !request.getReminderAt().isAfter(
                java.time.LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reminder date and time must be in the future"
            );
        }

        ReminderEvent event = new ReminderEvent(
                note.getId(),
                user.getId(),
                user.getEmail(),
                note.getTitle(),
                request.getReminderAt()
        );

        eventPublisher.publishEvent(event);

        logger.info(
                "Reminder request accepted: noteId={}, userId={}",
                note.getId(),
                user.getId()
        );
    }
}
