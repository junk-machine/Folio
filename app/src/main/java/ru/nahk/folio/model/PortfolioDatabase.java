package ru.nahk.folio.model;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import java.math.BigDecimal;

import ru.nahk.folio.utils.BigDecimalHelper;

/**
 * Provides access to the portfolio database.
 */
@Database(
    entities = {
        SymbolEntity.class,
        LotEntity.class,
        PositionEntity.class,
        GroupEntity.class,
        PortfolioItemWidgetEntity.class
    },
    version = 2
)
@TypeConverters({
    MoneyTypeConverter.class,
    TimestampTypeConverter.class
})
public abstract class PortfolioDatabase extends RoomDatabase {
    /**
     * Database migration that adds market capitalization column to symbols table.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        /**
         * Executes migration scripts.
         * @param database SQLite database instance.
         */
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE symbols ADD COLUMN market_cap INTEGER");
        }
    };

    /**
     * Synchronization object used during singleton initialization.
     */
    private static final Object instanceInitializationSyncObj = new Object();

    /**
     * Portfolio data store instance.
     */
    private static PortfolioDatabase instance;

    /**
     * Identifier of the newly created entity.
     * 0 will use auto-incremented ID.
     */
    public static final long NEW_ENTITY_ID = 0;

    /**
     * Retrieves data access object for groups information.
     * @return Data access object for groups information.
     */
    public abstract GroupDao groupDao();

    /**
     * Retrieves data access object for positions information.
     * @return Data access object for positions information.
     */
    public abstract PositionDao positionDao();

    /**
     * Retrieves data access object for symbols information.
     * @return Data access object for symbols information.
     */
    public abstract SymbolDao symbolDao();

    /**
     * Retrieves data access object for lots information.
     * @return Data access object for lots information.
     */
    public abstract LotDao lotDao();

    /**
     * Retrieves data access object for widgets information.
     * @return Data access object for widgets information.
     */
    public abstract WidgetDao widgetDao();

    /**
     * Retrieves an instance of the portfolio data store.
     * @return Instance of the portfolio data store
     */
    public static PortfolioDatabase getInstance(Context applicationContext) {
        if (instance == null) {
            synchronized (instanceInitializationSyncObj) {
                if (instance == null) {
                    instance = Room
                        .databaseBuilder(applicationContext, PortfolioDatabase.class, "folio-db")
                        .addCallback(new DatabaseInitializationCallback(applicationContext))
                        .addMigrations(MIGRATION_1_2)
                        .build();
                }
            }
        }

        return instance;
    }

    /**
     * Removes all groups and positions from portfolio.
     */
    public void clearPortfolio() {
        beginTransaction();
        try {
            positionDao().deleteAll();
            groupDao().deleteAll();
            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * Loads entire portfolio with the stored group expansion state.
     * @return Root portfolio group.
     */
    public GroupViewModel loadPortfolio() {
        return loadPortfolio(null);
    }

    /**
     * Loads entire portfolio with the given group expansion state.
     * @param isExpanded Flag that indicates whether all groups should be expanded.
     * @return Root portfolio group.
     */
    public GroupViewModel loadPortfolio(Boolean isExpanded) {
        return loadHierarchy(GroupDao.ROOT_GROUP_ID, isExpanded);
    }

    /**
     * Loads the group with given identifier including all its children.
     * @param groupId Identifier of the group to load.
     * @param isExpanded Flag that indicates whether all groups should be expanded.
     * @return Items group with given identifier.
     */
    private GroupViewModel loadHierarchy(long groupId, Boolean isExpanded) {
        beginTransaction();
        try {
            GroupViewModel group = groupDao().loadById(groupId);

            if (group != null) {
                BigDecimal[] value =
                    loadGroupChildren(group, (byte) 1, isExpanded);

                group.currentValue = value[0];
                group.baseValue = value[1];

                if (isExpanded != null) {
                    group.isExpanded = isExpanded;
                }
            }

            setTransactionSuccessful();

            return group;
        }
        finally {
            endTransaction();
        }
    }

    /**
     * Recursively loads all children in the given group.
     * @param group Group view model to load children for.
     * @param level Current hierarchy level.
     * @return Array with two elements where first element is the total value of all children
     * and second element is the total gain or loss of all children.
     */
    // TODO: Rewrite using recursive CTE?
    private BigDecimal[] loadGroupChildren(
            GroupViewModel group,
            byte level,
            Boolean isExpanded) {
        BigDecimal[] result = new BigDecimal[] { null, null };

        // Load children groups
        for (GroupViewModel childGroup: groupDao().loadByParentGroup(group.id)) {
            childGroup.level = level;
            childGroup.parent = group;
            group.children.add(childGroup);

            // Load children for the child group
            BigDecimal[] childGroupValue =
                loadGroupChildren(childGroup, (byte) (level + 1), isExpanded);

            // Populate group's value
            childGroup.currentValue = childGroupValue[0];
            childGroup.baseValue = childGroupValue[1];

            if (isExpanded != null) {
                childGroup.isExpanded = isExpanded;
            }

            // Update current group values
            result[0] = BigDecimalHelper.add(result[0], childGroupValue[0]);
            result[1] = BigDecimalHelper.add(result[1], childGroupValue[1]);
        }

        // Load children positions
        for (PositionViewModel childPosition: positionDao().loadByParentGroup(group.id)) {
            childPosition.level = level;
            childPosition.parent = group;
            group.children.add(childPosition);

            // Update current group values
            result[0] = BigDecimalHelper.add(result[0], childPosition.currentValue);
            result[1] = BigDecimalHelper.add(result[1], childPosition.baseValue);
        }

        if (!group.children.isEmpty()) {
            group.children.get(group.children.size() - 1).lastInParent = true;
        }

        return result;
    }
}
