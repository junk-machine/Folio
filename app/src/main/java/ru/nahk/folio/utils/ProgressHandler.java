package ru.nahk.folio.utils;

/**
 * Interface for an async task progress.
 */
public interface ProgressHandler {
    /**
     * Async task started.
     */
    void progressStart();

    /**
     * Async task ended.
     */
    void progressEnd();

    /**
     * Async task error.
     */
    void progressError(Exception error);
}
