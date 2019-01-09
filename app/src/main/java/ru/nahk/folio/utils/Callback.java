package ru.nahk.folio.utils;

/**
 * Callback with one generic argument.
 * @param <T> Type of the callback argument.
 */
public interface Callback<T> {
    /**
     * Executes the callback code.
     * @param arg Argument value.
     */
    void run(T arg);
}
