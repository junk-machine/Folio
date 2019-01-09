package ru.nahk.folio.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Data access methods to work with portfolio position entities.
 */
@Dao
public interface PositionDao {
    /**
     * Inserts new portfolio position entity.
     * @param position Portfolio position entity to insert.
     * @return Identifier of the newly inserted portfolio position.
     */
    @Insert
    long insert(PositionEntity position);

    /**
     * Moves portfolio position to new parent.
     * @param positionId Identifier of the portfolio position to move.
     * @param newParentId Identifier of the new parent group.
     */
    @Query("UPDATE positions SET parent_group_id = :newParentId WHERE id = :positionId")
    void move(long positionId, long newParentId);

    /**
     * Loads portfolio position with the given identifier.
     * @param positionId Identifier of the portfolio position.
     * @return Loaded portfolio position.
     */
    @Query(
        "SELECT " +
            "p.id AS id," +
            "p.symbol_id AS symbol_id," +
            "s.display_name AS name," +
            "SUM(l.quantity) AS quantity," +
            "CASE WHEN s.extended_time > s.latest_time" +
            "   THEN ifnull(SUM(l.quantity) * s.extended_price, s.extended_price)" +
            "   ELSE ifnull(SUM(l.quantity) * s.latest_price, s.latest_price)" +
            "END AS current_value," +
            "ifnull(SUM(l.quantity * l.purchase_price + l.commission), s.previous_close_price) AS base_value," +
            "CASE WHEN s.extended_time > s.latest_time" +
            "   THEN s.extended_price" +
            "   ELSE s.latest_price " +
            "END AS symbol_value," +
            "CASE WHEN s.extended_time > s.latest_time" +
            "   THEN s.extended_price - s.previous_close_price" +
            "   ELSE s.latest_price - s.previous_close_price " +
            "END AS symbol_value_change " +
        "FROM positions p " +
            "INNER JOIN symbols s ON s.id = p.symbol_id " +
            "LEFT OUTER JOIN lots l ON l.position_id = p.id " +
        "WHERE " +
            "p.id = :positionId " +
        "GROUP BY p.id;"
    )
    PositionViewModel load(long positionId);

    /**
     * Loads all direct children portfolio positions under given parent.
     * @param groupId Identifier of the parent group.
     * @return List of portfolio positions under given parent.
     */
    @Query(
        "SELECT " +
            "p.id AS id," +
            "p.symbol_id AS symbol_id," +
            "s.display_name AS name," +
            "SUM(l.quantity) AS quantity," +
            "CASE WHEN s.extended_time > s.latest_time" +
            "   THEN ifnull(SUM(l.quantity) * s.extended_price, s.extended_price)" +
            "   ELSE ifnull(SUM(l.quantity) * s.latest_price, s.latest_price)" +
            "END AS current_value," +
            "ifnull(SUM(l.quantity * l.purchase_price + l.commission), s.previous_close_price) AS base_value," +
            "CASE WHEN s.extended_time > s.latest_time" +
            "   THEN s.extended_price" +
            "   ELSE s.latest_price " +
            "END AS symbol_value," +
            "CASE WHEN s.extended_time > s.latest_time" +
            "   THEN s.extended_price - s.previous_close_price" +
            "   ELSE s.latest_price - s.previous_close_price " +
            "END AS symbol_value_change " +
        "FROM positions p " +
            "INNER JOIN symbols s ON s.id = p.symbol_id " +
            "LEFT OUTER JOIN lots l ON l.position_id = p.id " +
        "WHERE " +
            "p.parent_group_id = :groupId " +
        "GROUP BY p.id " +
        "ORDER BY ifnull(s.display_name, s.id);"
    )
    List<PositionViewModel> loadByParentGroup(long groupId);

    /**
     * Deletes portfolio position entity.
     * @param positionId Identifier of the portfolio position to delete.
     */
    @Query("DELETE FROM positions WHERE id = :positionId")
    void delete(long positionId);

    /**
     * Deletes all portfolio positions.
     */
    @Query("DELETE FROM positions")
    void deleteAll();
}
