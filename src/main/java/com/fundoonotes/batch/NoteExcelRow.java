package com.fundoonotes.batch;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Spring Batch and Excel Integration
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Represents a single row read from an Excel file.
 * Stores note information before it is converted into
 * the Note entity and saved into the database.
 */
public class NoteExcelRow {

    private Long userId;
    private String title;
    private String description;
    private boolean pinned;
    private boolean archived;
    private boolean trashed;

    /**
     * Returns the ID of the user who owns the note.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who owns the note.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the title of the note.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the note.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of the note.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the note.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns whether the note is pinned.
     */
    public boolean isPinned() {
        return pinned;
    }

    /**
     * Sets the pinned status of the note.
     */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    /**
     * Returns whether the note is archived.
     */
    public boolean isArchived() {
        return archived;
    }

    /**
     * Sets the archived status of the note.
     */
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    /**
     * Returns whether the note is in the trash.
     */
    public boolean isTrashed() {
        return trashed;
    }

    /**
     * Sets the trashed status of the note.
     */
    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }
}