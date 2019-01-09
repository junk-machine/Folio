package ru.nahk.folio.activities;

import ru.nahk.folio.model.SymbolEntity;

/**
 * Interface for an activity that can display symbol information.
 */
public interface SymbolDetailsPresenter {
    /**
     * Sets the data to be presented.
     * @param symbolEntity Information about the symbol to display.
     */
    void setData(SymbolEntity symbolEntity);
}
