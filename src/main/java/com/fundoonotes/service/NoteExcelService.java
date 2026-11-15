package com.fundoonotes.service;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Excel Export Service
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Defines operations for exporting user notes
 * into an Excel workbook.
 */
public interface NoteExcelService {

    /**
     * Exports all notes belonging to a user.
     *
     * @param userId ID of the user
     * @return generated Excel file as byte array
     */
    byte[] exportNotes(Long userId) throws Exception;
}