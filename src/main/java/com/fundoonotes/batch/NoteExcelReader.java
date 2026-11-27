package com.fundoonotes.batch;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.ItemStreamException;
/*
 * Project Name : Fundoo Notes Backend
 * Description  : Reads XLSX note rows and releases workbook resources.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
public class NoteExcelReader
        implements ItemReader<NoteExcelRow>, ItemStream {

    private final String filePath;

    private Workbook workbook;
    private FileInputStream inputStream;
    private Iterator<Row> rowIterator;

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Creates an Excel reader for the supplied file path.
     */
    public NoteExcelReader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads the next available Excel row.
     * Returns null when all rows have been processed.
     */
    @Override
    public NoteExcelRow read() throws Exception {

        if (workbook == null) {
            initializeWorkbook();
        }

        if (rowIterator == null || !rowIterator.hasNext()) {
            closeWorkbook();
            return null;
        }

        Row row = rowIterator.next();

        return convertRow(row);
    }

    /**
     * Releases workbook resources when the step completes or fails.
     */
    @Override
    public void close() {

        try {
            closeWorkbook();
        } catch (IOException exception) {
            throw new ItemStreamException(
                    "Unable to close Excel workbook", exception);
        }
    }

    /**
     * Opens the Excel workbook and prepares its first sheet.
     */
    private void initializeWorkbook() throws IOException {

        inputStream = new FileInputStream(filePath);

        try {
            workbook = WorkbookFactory.create(inputStream);
            if (!(workbook instanceof XSSFWorkbook)) {
                throw new IllegalArgumentException(
                        "Only .xlsx workbook content is supported");
            }

            Sheet sheet = workbook.getSheetAt(0);

            rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
        } catch (IOException | RuntimeException exception) {
            try {
                closeWorkbook();
            } catch (IOException closeException) {
                exception.addSuppressed(closeException);
            }
            throw exception;
        }
    }

    /**
     * Converts an Excel row into a NoteExcelRow object.
     */
    private NoteExcelRow convertRow(Row row) {

        NoteExcelRow note = new NoteExcelRow();

        note.setUserId(parseLong(getCellValue(row, 0)));
        note.setTitle(getCellValue(row, 1));
        note.setDescription(getCellValue(row, 2));
        note.setPinned(parseBoolean(getCellValue(row, 3)));
        note.setArchived(parseBoolean(getCellValue(row, 4)));
        note.setTrashed(parseBoolean(getCellValue(row, 5)));

        return note;
    }

    /**
     * Reads the content of a cell as a string.
     */
    private String getCellValue(Row row, int columnIndex) {

        if (row.getCell(columnIndex) == null) {
            return "";
        }

        return formatter.formatCellValue(
                row.getCell(columnIndex)).trim();
    }

    /**
     * Converts a string into a Long value.
     */
    private Long parseLong(String value) {

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Converts a string into a boolean value.
     * Rejects values other than true or false.
     */
    private boolean parseBoolean(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw new IllegalArgumentException(
                "Boolean value must be true or false");
    }

    /**
     * Closes the workbook and input stream.
     */
    private void closeWorkbook() throws IOException {

        if (workbook != null) {
            workbook.close();
            workbook = null;
        }

        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }
}