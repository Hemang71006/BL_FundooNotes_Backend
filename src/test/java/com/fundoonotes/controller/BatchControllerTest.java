package com.fundoonotes.controller;

import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.NoteExcelService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies batch endpoint authentication and ownership checks.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
class BatchControllerTest {

    @Test
    void oversizedImportIsRejectedBeforeStartingJob() throws Exception {
        JobOperator jobOperator = mock(JobOperator.class);
        NoteExcelService noteExcelService = mock(NoteExcelService.class);
        UserRepository userRepository = mock(UserRepository.class);
        User authenticatedUser = new User();
        authenticatedUser.setId(7L);
        authenticatedUser.setEmail("owner@example.com");
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(authenticatedUser));

        BatchController controller = new BatchController(
                jobOperator,
                mock(JobRepository.class),
                mock(Job.class),
                noteExcelService,
                userRepository);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("owner@example.com")
                .password("unused")
                .authorities("USER")
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[1_048_577]);

        var response = controller.importNotes(file, userDetails);

        assertEquals(413, response.getStatusCode().value());
        verify(jobOperator, never()).start(any(), any(JobParameters.class));
    }

    @Test
    void importJobUsesAuthenticatedUserId() throws Exception {
        JobOperator jobOperator = mock(JobOperator.class);
        Job importJob = mock(Job.class);
        NoteExcelService noteExcelService = mock(NoteExcelService.class);
        UserRepository userRepository = mock(UserRepository.class);
        User authenticatedUser = new User();
        authenticatedUser.setId(7L);
        authenticatedUser.setEmail("owner@example.com");
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(authenticatedUser));
        JobExecution jobExecution = mock(JobExecution.class);
        when(jobExecution.getId()).thenReturn(12L);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.STARTING);
        when(jobOperator.start(eq(importJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        BatchController controller = new BatchController(
                jobOperator,
                mock(JobRepository.class),
                importJob,
                noteExcelService,
                userRepository);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("owner@example.com")
                .password("unused")
                .authorities("USER")
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "..\\..\\outside.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1});

        var parameters = org.mockito.ArgumentCaptor
                .forClass(JobParameters.class);
        Path uploadedFile = null;
        try {
            controller.importNotes(file, userDetails);

            verify(jobOperator).start(eq(importJob), parameters.capture());
            assertEquals(7L, parameters.getValue().getLong("userId"));
            uploadedFile = Path.of(parameters.getValue().getString("filePath"));
            assertTrue(uploadedFile.getFileName().toString().startsWith("fundoo-notes-"));
        } finally {
            if (uploadedFile != null) {
                Files.deleteIfExists(uploadedFile);
            }
        }
    }

    @Test
    void exportRejectsDifferentPathUserId() throws Exception {
        JobOperator jobOperator = mock(JobOperator.class);
        NoteExcelService noteExcelService = mock(NoteExcelService.class);
        UserRepository userRepository = mock(UserRepository.class);
        User authenticatedUser = new User();
        authenticatedUser.setId(7L);
        authenticatedUser.setEmail("owner@example.com");
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(authenticatedUser));

        BatchController controller = new BatchController(
                jobOperator,
                mock(JobRepository.class),
                mock(Job.class),
                noteExcelService,
                userRepository);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("owner@example.com")
                .password("unused")
                .authorities("USER")
                .build();

        var response = controller.exportNotes(99L, userDetails);

        assertEquals(404, response.getStatusCode().value());
        verify(noteExcelService, never()).exportNotes(any(Long.class));
    }

    @Test
    void cleanupTemporaryImportWhenJobStartupFails() throws Exception {
        JobOperator jobOperator = mock(JobOperator.class);
        Job importJob = mock(Job.class);
        NoteExcelService noteExcelService = mock(NoteExcelService.class);
        UserRepository userRepository = mock(UserRepository.class);
        User authenticatedUser = new User();
        authenticatedUser.setId(7L);
        authenticatedUser.setEmail("owner@example.com");
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(authenticatedUser));
        when(jobOperator.start(eq(importJob), any(JobParameters.class)))
                .thenThrow(new IllegalStateException("job startup failed"));

        BatchController controller = new BatchController(
                jobOperator,
                mock(JobRepository.class),
                importJob,
                noteExcelService,
                userRepository);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("owner@example.com")
                .password("unused")
                .authorities("USER")
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1});

        var parameters = org.mockito.ArgumentCaptor
                .forClass(JobParameters.class);
        Path uploadedFile = null;
        try {
            assertThrows(IllegalStateException.class,
                    () -> controller.importNotes(file, userDetails));

            verify(jobOperator).start(eq(importJob), parameters.capture());
            uploadedFile = Path.of(parameters.getValue().getString("filePath"));
            assertFalse(Files.exists(uploadedFile));
        } finally {
            if (uploadedFile != null) {
                Files.deleteIfExists(uploadedFile);
            }
        }
    }

    @Test
    void jobStatusIsHiddenFromDifferentAuthenticatedUser() {
        JobOperator jobOperator = mock(JobOperator.class);
        JobRepository jobRepository = mock(JobRepository.class);
        NoteExcelService noteExcelService = mock(NoteExcelService.class);
        UserRepository userRepository = mock(UserRepository.class);
        User authenticatedUser = new User();
        authenticatedUser.setId(7L);
        authenticatedUser.setEmail("owner@example.com");
        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(authenticatedUser));

        JobExecution jobExecution = mock(JobExecution.class);
        when(jobExecution.getJobParameters()).thenReturn(
                new org.springframework.batch.core.job.parameters.JobParametersBuilder()
                        .addLong("userId", 99L)
                        .toJobParameters());
        when(jobRepository.getJobExecution(12L)).thenReturn(jobExecution);

        BatchController controller = new BatchController(
                jobOperator,
                jobRepository,
                mock(Job.class),
                noteExcelService,
                userRepository);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("owner@example.com")
                .password("unused")
                .authorities("USER")
                .build();

        var response = controller.getJobStatus(12L, userDetails);

        assertEquals(404, response.getStatusCode().value());
    }
}
