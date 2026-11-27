
package com.fundoonotes.controller;

import com.fundoonotes.service.NoteExcelService;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Exposes owner-scoped Excel import, export and job status APIs.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private static final long MAX_IMPORT_FILE_BYTES = 1_048_576;

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Job importNotesJob;
    private final NoteExcelService noteExcelService;
    private final UserRepository userRepository;

    /**
     * Initializes the controller with batch and Excel services.
     */
    public BatchController(
            JobOperator jobOperator,
            JobRepository jobRepository,
            @Qualifier("importNotesJob") Job importNotesJob,
            NoteExcelService noteExcelService,
            UserRepository userRepository) {

        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.importNotesJob = importNotesJob;
        this.noteExcelService = noteExcelService;
        this.userRepository = userRepository;
    }

    /**
     * Accepts an Excel file and starts the import job.
     *
     * @param file uploaded XLSX file
     * @return batch execution information
     */
    @PostMapping("/import")
    public ResponseEntity<?> importNotes(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Long authenticatedUserId = getAuthenticatedUserId(userDetails);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please upload an Excel file"));
        }

        String filename = file.getOriginalFilename();

        if (filename == null
                || !filename.toLowerCase().endsWith(".xlsx")) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only .xlsx files are supported"));
        }

        if (file.getSize() > MAX_IMPORT_FILE_BYTES) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("message", "Excel files must not exceed 1 MiB"));
        }

        Path tempFile = Files.createTempFile(
                "fundoo-notes-", ".xlsx");

        JobExecution execution;
        try {
            file.transferTo(tempFile);

            JobParameters parameters = new JobParametersBuilder()
                    .addString(
                            "filePath",
                            tempFile.toAbsolutePath().toString())
                    .addLong(
                            "timestamp",
                            System.currentTimeMillis())
                    .addLong("userId", authenticatedUserId)
                    .toJobParameters();

            execution = jobOperator.start(importNotesJob, parameters);
        } catch (Exception exception) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (java.io.IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }

        return ResponseEntity.accepted().body(Map.of(
                "message", "Excel import job started",
                "jobExecutionId", execution.getId(),
                "status", execution.getStatus().toString()
        ));
    }

    /**
     * Retrieves the status of a batch job execution.
     *
     * @param executionId batch execution ID
     * @return job execution status
     */
    @GetMapping("/status/{executionId}")
    public ResponseEntity<?> getJobStatus(
            @PathVariable Long executionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        JobExecution execution =
                jobRepository.getJobExecution(executionId);

        if (execution == null) {
            return ResponseEntity.notFound().build();
        }

        Long authenticatedUserId = getAuthenticatedUserId(userDetails);
        Long executionUserId =
                execution.getJobParameters().getLong("userId");
        if (!authenticatedUserId.equals(executionUserId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "jobExecutionId", execution.getId(),
                "status", execution.getStatus().toString(),
                "exitStatus",
                execution.getExitStatus().getExitCode()
        ));
    }

    /**
     * Downloads the notes belonging to a user as an XLSX file.
     *
     * @param userId ID of the user
     * @return downloadable Excel file
     */
    @GetMapping({"/export", "/export/{userId}"})
    public ResponseEntity<byte[]> exportNotes(
            @PathVariable(required = false) Long userId,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Long authenticatedUserId = getAuthenticatedUserId(userDetails);
        if (userId != null && !userId.equals(authenticatedUserId)) {
            return ResponseEntity.notFound().build();
        }

        byte[] excelFile =
                noteExcelService.exportNotes(authenticatedUserId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=fundoo-notes.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
