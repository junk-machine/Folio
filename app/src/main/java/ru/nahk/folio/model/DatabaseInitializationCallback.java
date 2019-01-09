package ru.nahk.folio.model;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.RoomDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import ru.nahk.folio.R;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

/**
 * Database initialization callback that populates initial data.
 */
public class DatabaseInitializationCallback extends RoomDatabase.Callback {
    /**
     * Application context.
     */
    private final Context mContext;

    /**
     * Creates new instance of the {@link DatabaseInitializationCallback} class
     * with the provided application context.
     * @param context Application context.
     */
    public DatabaseInitializationCallback(@NonNull Context context) {
        mContext = context;
    }

    /**
     * Inserts root group into newly created database.
     * @param db SQLite database.
     */
    @Override
    public void onCreate(@NonNull SupportSQLiteDatabase db) {
        super.onCreate(db);

        ensureRootGroup(db);
    }

    /**
     * Ensures that root group exists in the database.
     * @param db SQLite database.
     */
    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db) {
        super.onOpen(db);

        ensureRootGroup(db);
    }

    /**
     * Tries to insert root group into database.
     * @param db SQLite database.
     */
    private void ensureRootGroup(SupportSQLiteDatabase db) {
        ContentValues rootGroup = new ContentValues();

        rootGroup.put(GroupEntity.ID_COLUMN, GroupDao.ROOT_GROUP_ID);
        rootGroup.put(GroupEntity.NAME_COLUMN, mContext.getString(R.string.root_group_name));
        rootGroup.put(GroupEntity.IS_EXPANDED_COLUMN, 1);
        rootGroup.put(GroupEntity.PARENT_GROUP_ID_COLUMN, (Integer) null);

        db.insert(GroupEntity.TABLE_NAME, CONFLICT_IGNORE, rootGroup);
    }
}
