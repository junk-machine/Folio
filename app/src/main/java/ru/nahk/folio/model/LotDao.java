package ru.nahk.folio.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Data access methods to work with lot entities.
 */
@Dao
public interface LotDao {
    /**
     * Loads all lots for the given position.
     * @param positionId Identifier of the position.
     * @return List of lots for the given position.
     */
    @Query(
        "SELECT " +
            "l.id AS id," +
            "l.quantity AS quantity," +
            "l.purchase_date AS purchase_date," +
            "l.purchase_price AS purchase_price," +
            "l.commission AS commission," +
            "CASE WHEN s.extended_time > s.latest_time" +
            "   THEN l.quantity * s.extended_price" +
            "   ELSE l.quantity * s.latest_price " +
            "END AS current_value," +
            "l.quantity * l.purchase_price + l.commission AS base_value " +
        "FROM lots l " +
            "INNER JOIN positions p ON p.id = l.position_id " +
            "INNER JOIN symbols s ON s.id = p.symbol_id " +
        "WHERE l.position_id = :positionId " +
        "ORDER BY l.purchase_date DESC;"
    )
    List<LotViewModel> getLotsForPosition(long positionId);

    /**
     * Inserts new lot entity.
     * @param lot Lot entity to insert.
     * @return Identifier of the newly created lot.
     */
    @Insert
    long insert(LotEntity lot);

    /**
     * Updates lot entity.
     * @param lot Lot entity to update.
     */
    @Update
    void update(LotEntity lot);

    /**
     * Deletes lot entity.
     * @param lotId Identifier of the lot to delete.
     */
    @Query("DELETE FROM lots WHERE id = :lotId")
    void delete(long lotId);
}
