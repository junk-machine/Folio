package ru.nahk.folio.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

/**
 * Data access methods to work with symbol entities.
 */
@Dao
public interface SymbolDao {
    /**
     * Inserts new symbol entity.
     * @param symbol Symbol entity to insert.
     */
    @Insert
    void insert(SymbolEntity symbol);

    /**
     * Ensure that symbol entity exists.
     * @param symbol Symbol entity to verify.
     * @return Index of inserted row or -1, if no rows were inserted..
     */
    @Insert(onConflict = IGNORE)
    long ensureSymbol(SymbolEntity symbol);

    /**
     * Loads all symbol entities.
     * @return List of all symbol entities.
     */
    @Query("SELECT * FROM symbols")
    List<SymbolEntity> get();

    /**
     * Loads symbol entity with the given identifier.
     * @param symbolId Identifier of the symbol entity to load.
     * @return Loaded symbol entity.
     */
    @Query("SELECT * FROM symbols WHERE id = :symbolId")
    SymbolEntity get(String symbolId);

    /**
     * Updates multiple symbol entities.
     * @param symbols Symbol entities to update.
     */
    @Update
    void update(SymbolEntity... symbols);

    /**
     * Updates lies of symbol entities.
     * @param symbols Symbol entities to update.
     */
    @Update
    void update(List<SymbolEntity> symbols);

    /**
     * Deletes symbol entity
     * @param symbol Symbol entity to delete.
     */
    @Delete
    void delete(SymbolEntity symbol);

    /**
     * Removes symbol rows that are not referenced.
     */
    @Query("DELETE FROM symbols WHERE id NOT IN (SELECT DISTINCT symbol_id FROM positions)")
    void cleanupSymbols();
}
