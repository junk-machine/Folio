package ru.nahk.folio.activities;

import android.support.v7.app.AppCompatActivity;

import ru.nahk.folio.model.PortfolioDatabase;

/**
 * Base class for activities that require access to persisted data store.
 */
public abstract class DataAccessActivityBase extends AppCompatActivity {
    /**
     * Retrieves an instance of the persisted data store.
     * @return Instance of the persisted data store
     */
    protected PortfolioDatabase getDataStore() {
        return PortfolioDatabase.getInstance(getApplicationContext());
    }
}
