package com.fundoonotes.service.impl;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.fundoonotes.service.NoteExcelService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Excel Export Service
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Exports notes belonging to a user into an Excel workbook.
 * Generates an XLSX file using Apache POI.
 */
@Service
public class NoteExcelServiceImpl implements NoteExcelService {

    private final NoteRepository noteRepository;

    /**
     * Initializes the Excel service with NoteRepository.
     */
    public NoteExcelServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Fetches a user's notes and generates an Excel file.
     *
     * @param userId ID of the user whose notes are exported
     * @return Excel workbook as a byte array
     */
    @Transactional(readOnly = true)
    public byte[] exportNotes(Long userId) throws IOException {

        List<Note> notes = noteRepository.findAllByUser_Id(userId);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Notes");

            // Create the Excel header row.
            Row header = sheet.createRow(0);

            String[] columns = {
                    "id",
                    "title",
                    "description",
                    "pinned",
                    "archived",
                    "trashed",
                    "createdAt",
                    "updatedAt"
            };

            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            // Add each note to the Excel worksheet.
            int rowIndex = 1;

            for (Note note : notes) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(note.getId());
                row.createCell(1).setCellValue(note.getTitle());

                row.createCell(2).setCellValue(
                        note.getDescription() == null
                                ? ""
                                : note.getDescription());

                row.createCell(3).setCellValue(note.isPinned());
                row.createCell(4).setCellValue(note.isArchived());
                row.createCell(5).setCellValue(note.isTrashed());

                row.createCell(6).setCellValue(
                        note.getCreatedAt().toString());

                row.createCell(7).setCellValue(
                        note.getUpdatedAt().toString());
            }

            // Automatically adjust column widths.
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the workbook into the output stream.
            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }
}
