
package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.JobExecutionListener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Configures the Excel import job and temporary file cleanup.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
@Configuration
public class BatchConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(BatchConfig.class);

    /**
     * Creates an Excel reader for each job execution.
     * The Excel file path is supplied through job parameters.
     */
    @Bean
    @StepScope
    public NoteExcelReader noteExcelReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        return new NoteExcelReader(filePath);
    }

    @Bean
    @StepScope
    public NoteItemProcessor noteItemProcessor(
            EntityManager entityManager,
            @Value("#{jobParameters['userId']}") Long userId) {

        return new NoteItemProcessor(entityManager, userId);
    }

    @Bean
    public JobExecutionListener deleteImportFileAfterJob() {
        return new JobExecutionListener() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                String filePath =
                        jobExecution.getJobParameters().getString("filePath");
                if (filePath == null) {
                    return;
                }

                try {
                    Files.deleteIfExists(Path.of(filePath));
                } catch (IOException exception) {
                    logger.error("Unable to remove completed batch import file", exception);
                }
            }
        };
    }

    /**
     * Creates the step that reads, processes,
     * and writes note records in chunks of 100.
     */
    @Bean
    public Step importNotesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            NoteExcelReader noteExcelReader,
            NoteItemProcessor noteItemProcessor,
            NoteItemWriter noteItemWriter) {

        return new StepBuilder("importNotesStep", jobRepository)
                .<NoteExcelRow, Note>chunk(100)
                .transactionManager(transactionManager)
                .reader(noteExcelReader)
                .processor(noteItemProcessor)
                .writer(noteItemWriter)
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skipLimit(100)
                .build();
    }

    /**
     * Creates the Excel import job.
     * The job starts by executing the import step.
     */
    @Bean
    public Job importNotesJob(
            JobRepository jobRepository,
            Step importNotesStep,
            JobExecutionListener deleteImportFileAfterJob) {

        return new JobBuilder("importNotesJob", jobRepository)
                .start(importNotesStep)
                .listener(deleteImportFileAfterJob)
                .build();
    }
}
