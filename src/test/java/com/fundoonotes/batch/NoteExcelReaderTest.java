package com.fundoonotes.batch;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies XLSX validation and workbook resource cleanup.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
class NoteExcelReaderTest {

    @Test
    void rejectsMalformedWorkbookAndClosesInputFile() throws Exception {
        Path malformedFile =
                Files.createTempFile("fundoo-notes-invalid-", ".xlsx");
        Files.writeString(malformedFile, "not an Excel workbook");

        NoteExcelReader reader = new NoteExcelReader(malformedFile.toString());

        assertThrows(Exception.class, reader::read);
        reader.close();

        Files.delete(malformedFile);
    }

    @Test
    void closesWorkbookWhenRowProcessingFails() throws Exception {
        Path invalidRowFile =
                Files.createTempFile("fundoo-notes-invalid-row-", ".xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Notes");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("userId");
            header.createCell(1).setCellValue("title");
            header.createCell(2).setCellValue("description");
            header.createCell(3).setCellValue("pinned");

            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("1");
            row.createCell(1).setCellValue("Valid title");
            row.createCell(2).setCellValue("Valid description");
            row.createCell(3).setCellValue("maybe");

            try (var output = Files.newOutputStream(invalidRowFile)) {
                workbook.write(output);
            }
        }

        NoteExcelReader reader =
                new NoteExcelReader(invalidRowFile.toString());

        assertThrows(IllegalArgumentException.class, reader::read);

        reader.close();

        Files.delete(invalidRowFile);
    }

    @Test
    void closesWorkbookAfterReadingLastRow() throws Exception {
        Path validFile =
                Files.createTempFile("fundoo-notes-valid-", ".xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Notes");
            var header = sheet.createRow(0);
            header.createCell(1).setCellValue("title");

            var row = sheet.createRow(1);
            row.createCell(1).setCellValue("Imported note");

            try (var output = Files.newOutputStream(validFile)) {
                workbook.write(output);
            }
        }

        NoteExcelReader reader = new NoteExcelReader(validFile.toString());

        assertNotNull(reader.read());
        assertNull(reader.read());
        reader.close();

        Files.delete(validFile);
    }

    @Test
    void rejectsLegacyXlsWorkbookContent() throws Exception {
        Path legacyFile =
                Files.createTempFile("fundoo-notes-legacy-", ".xlsx");

        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            workbook.createSheet("Notes").createRow(0).createCell(0)
                    .setCellValue("title");

            try (var output = Files.newOutputStream(legacyFile)) {
                workbook.write(output);
            }
        }

        NoteExcelReader reader = new NoteExcelReader(legacyFile.toString());

        assertThrows(IllegalArgumentException.class, reader::read);
        reader.close();

        Files.delete(legacyFile);
    }
}
