package ru.nahk.folio.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Data access methods to work with widget entities.
 */
@Dao
public interface WidgetDao {
    /**
     * Inserts new portfolio item widget entity.
     * @param portfolioItemWidget Portfolio item widget entity to insert.
     */
    @Insert
    void insert(PortfolioItemWidgetEntity portfolioItemWidget);

    /**
     * Loads portfolio item widget entity with given identifier.
     * @param widgetId Unique identifier of the widget entity to load.
     * @return Loaded portfolio item widget entity.
     */
    @Query("SELECT * FROM portfolio_item_widgets WHERE id = :widgetId")
    PortfolioItemWidgetEntity loadPortfolioItemWidget(int widgetId);

    /**
     * Loads all portfolio item widget entities.
     * @return Loaded portfolio item widgets entity.
     */
    @Query("SELECT * FROM portfolio_item_widgets")
    List<PortfolioItemWidgetEntity> loadAllPortfolioItemWidgets();

    /**
     * Deletes multiple portfolio item widget entities with given identifiers.
     * @param widgetIds Unique identifiers of the widget entities to delete.
     */
    @Query("DELETE FROM portfolio_item_widgets WHERE id IN (:widgetIds)")
    void deletePortfolioItemWidgets(int[] widgetIds);
}
