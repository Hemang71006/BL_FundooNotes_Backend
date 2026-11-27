package com.fundoonotes.batch;

import org.junit.jupiter.api.Test;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies batch listener cleanup of temporary import files.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
class BatchConfigTest {

    @Test
    void deletesTemporaryImportAfterJobCompletes() throws Exception {
        Path importFile = Files.createTempFile("fundoo-notes-test-", ".xlsx");
        JobExecution execution = executionFor(importFile, BatchStatus.COMPLETED);

        new BatchConfig().deleteImportFileAfterJob().afterJob(execution);

        assertFalse(Files.exists(importFile));
    }

    @Test
    void deletesTemporaryImportAfterJobFails() throws Exception {
        Path importFile = Files.createTempFile("fundoo-notes-failed-", ".xlsx");
        JobExecution execution = executionFor(importFile, BatchStatus.FAILED);

        new BatchConfig().deleteImportFileAfterJob().afterJob(execution);

        assertFalse(Files.exists(importFile));
    }

    private JobExecution executionFor(Path importFile, BatchStatus status) {
        JobExecution execution = mock(JobExecution.class);
        when(execution.getJobParameters()).thenReturn(
                new JobParametersBuilder()
                        .addString("filePath", importFile.toString())
                        .toJobParameters());
        when(execution.getStatus()).thenReturn(status);
        return execution;
    }
}
