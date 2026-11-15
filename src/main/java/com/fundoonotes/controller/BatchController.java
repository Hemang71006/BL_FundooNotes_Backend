
package com.fundoonotes.controller;

import com.fundoonotes.service.NoteExcelService;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Batch REST Controller
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Provides REST APIs for Excel import, Excel export,
 * and Spring Batch job status monitoring.
 */
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Job importNotesJob;
    private final NoteExcelService noteExcelService;

    /**
     * Initializes the controller with batch and Excel services.
     */
    public BatchController(
            JobOperator jobOperator,
            JobRepository jobRepository,
            @Qualifier("importNotesJob") Job importNotesJob,
            NoteExcelService noteExcelService) {

        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.importNotesJob = importNotesJob;
        this.noteExcelService = noteExcelService;
    }

    /**
     * Accepts an Excel file and starts the import job.
     *
     * @param file uploaded XLSX file
     * @return batch execution information
     */
    @PostMapping("/import")
    public ResponseEntity<?> importNotes(
            @RequestParam("file") MultipartFile file) throws Exception {

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

        Path tempFile = Files.createTempFile(
                "fundoo-notes-", ".xlsx");

        file.transferTo(tempFile);

        JobParameters parameters = new JobParametersBuilder()
                .addString(
                        "filePath",
                        tempFile.toAbsolutePath().toString())
                .addLong(
                        "timestamp",
                        System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobOperator.start(
                importNotesJob,
                parameters);

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
            @PathVariable Long executionId) {

        JobExecution execution =
                jobRepository.getJobExecution(executionId);

        if (execution == null) {
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
    @GetMapping("/export/{userId}")
    public ResponseEntity<byte[]> exportNotes(
            @PathVariable Long userId) throws Exception {

        byte[] excelFile =
                noteExcelService.exportNotes(userId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=fundoo-notes.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }
}
