
package com.fundoonotes.batch;

import com.fundoonotes.entity.Note;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Spring Batch Configuration
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Configures the Spring Batch job and step.
 * Defines the Excel reader, processor, and writer.
 * Uses chunk-oriented processing to import Excel records.
 */
@Configuration
public class BatchConfig {

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
            Step importNotesStep) {

        return new JobBuilder("importNotesJob", jobRepository)
                .start(importNotesStep)
                .build();
    }
}
