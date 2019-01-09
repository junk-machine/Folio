package ru.nahk.folio.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomWarnings;

import java.util.List;

/**
 * Data access methods to work with group entities.
 */
@Dao
public interface GroupDao {
    /**
     * Identifier of the root items group.
     */
    long ROOT_GROUP_ID = 1;

    /**
     * Inserts new group entity.
     * @param group Group entity to insert.
     * @return Identifier of newly inserted group.
     */
    @Insert
    long insert(GroupEntity group);

    /**
     * Renames a group.
     * @param groupId Identifier of the group to rename.
     * @param newName New name for the group.
     */
    @Query("UPDATE groups SET name = :newName WHERE id = :groupId")
    void rename(long groupId, String newName);

    /**
     * Moves group to new parent.
     * @param groupId Identifier of the group to move.
     * @param newParentId Identifier of the new parent group.
     */
    @Query("UPDATE groups SET parent_group_id = :newParentId WHERE id = :groupId")
    void move(long groupId, long newParentId);

    /**
     * Deletes a group.
     * @param groupId Identifier of the group to delete.
     */
    @Query("DELETE FROM groups WHERE id = :groupId AND id != 0")
    void delete(long groupId);

    /**
     * Deletes all groups.
     */
    @Query("DELETE FROM groups WHERE id != 1")
    void deleteAll();

    /**
     * Sets group expansion state.
     * @param groupId Identifier of the group to update.
     * @param isExpanded New grooup expansion state.
     */
    @Query("UPDATE groups SET is_expanded = :isExpanded WHERE id = :groupId")
    void setGroupExpandedState(long groupId, boolean isExpanded);

    /**
     * Loads group entity with given identifier.
     * @param groupId Identifier of the group to load.
     * @return Loaded group entity.
     */
    // GroupViewModel has two extra fields (currentValue, baseValue) that will be computed in the code
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, name, is_expanded FROM groups WHERE id = :groupId")
    GroupViewModel loadById(long groupId);

    /**
     * Loads all direct children groups under given parent.
     * @param groupId Identifier of the parent group.
     * @return List of groups under given parent.
     */
    // GroupViewModel has two extra fields (currentValue, baseValue) that will be computed in the code
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(
        "SELECT " +
            "g.id AS id," +
            "g.name AS name," +
            "g.is_expanded AS is_expanded " +
        "FROM groups g " +
        "WHERE " +
            "g.parent_group_id == :groupId " +
            "OR (:groupId IS NULL AND g.parent_group_id IS NULL)" +
        "ORDER BY g.name;"
    )
    List<GroupViewModel> loadByParentGroup(long groupId);

    /**
     * Loads positions group with computed value.
     * @param groupId Identifier of the group to load.
     * @return Loaded group with curent and basse values.
     */
    @Query(
        "WITH RECURSIVE nested_groups(id) AS (" +
            "VALUES (:groupId) " +
            "UNION ALL " +
            "SELECT g.id " +
            "FROM nested_groups ng " +
                "INNER JOIN groups g ON g.parent_group_id = ng.id)," +
        "nested_positions AS (" +
            "SELECT " +
                "CASE WHEN s.extended_time > s.latest_time " +
                    "THEN ifnull(SUM(l.quantity) * s.extended_price, s.extended_price) " +
                    "ELSE ifnull(SUM(l.quantity) * s.latest_price, s.latest_price) " +
                "END AS current_value," +
                "ifnull(SUM(l.quantity * l.purchase_price + l.commission), s.previous_close_price) AS base_value " +
            "FROM positions p " +
                "INNER JOIN nested_groups ng ON ng.id = p.parent_group_id " +
                "INNER JOIN symbols s ON s.id = p.symbol_id " +
                "LEFT OUTER JOIN lots l ON l.position_id = p.id " +
            "GROUP BY p.id" +
        ")," +
        "group_value AS (" +
            "SELECT " +
                "SUM(current_value) AS current_value," +
                "SUM(base_value) AS base_value " +
            "FROM nested_positions" +
        ")" +
        "SELECT g.id, g.name, g.is_expanded, gv.current_value, gv.base_value " +
        "FROM groups g, group_value gv " +
        "WHERE g.id = :groupId")
    GroupViewModel loadWithValue(long groupId);
}
