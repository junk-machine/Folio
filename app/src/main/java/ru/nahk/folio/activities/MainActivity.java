package ru.nahk.folio.activities;

import android.content.ClipData;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import ru.nahk.folio.R;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PortfolioItemViewModel;
import ru.nahk.folio.model.PositionViewModel;
import ru.nahk.folio.model.PositionsListItemViewModel;
import ru.nahk.folio.tasks.AddGroupTask;
import ru.nahk.folio.tasks.AddPositionTask;
import ru.nahk.folio.tasks.DeletePositionsListItemTask;
import ru.nahk.folio.tasks.LoadPortfolioTask;
import ru.nahk.folio.tasks.MovePositionsListItemTask;
import ru.nahk.folio.tasks.RefreshPortfolioSymbolsDataTask;
import ru.nahk.folio.tasks.RefreshPositionsListTask;
import ru.nahk.folio.tasks.RenameGroupTask;
import ru.nahk.folio.utils.Callback;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Main application activity.
 */
public class MainActivity
    extends
        SymbolsDataTrackingActivity
    implements
        ProgressHandler,
        SwipeRefreshLayout.OnRefreshListener,
        PositionsListAdapter.OnItemLongClickListener,
        PositionsListAdapter.OnGroupItemClickListener,
        PositionsListAdapter.OnPositionItemClickListener,
        GroupSwipeController.GroupActionsHandler,
        MovePositionsListItemDragListener.OnPositionsListItemMovedListener,
        DeleteItemDragListener.OnItemDeletedListener<PositionsListItemViewModel> {

    /**
     * List control that displays portfolio positions and groups.
     */
    private RecyclerView mPositionsList;

    /**
     * Layout control that enables swipe-to-refresh functionality.
     */
    private SwipeRefreshLayout mSwipeRefresh;

    /**
     * Positions view list adapter.
     */
    private PositionsListAdapter mAdapter;

    /**
     * Initializes the activity.
     * @param savedInstanceState Previous activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        int animationDuration =
            getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Setup delete position button
        findViewById(R.id.button_delete).setOnDragListener(
            new DeleteItemDragListener<>(this, animationDuration));

        // Initialize RecyclerView
        mPositionsList = findViewById(R.id.positions_list_view);
        mPositionsList.setHasFixedSize(true);
        mPositionsList.setLayoutManager(new LinearLayoutManager(this));

        MovePositionsListItemDragListener moveItemDragListener =
            new MovePositionsListItemDragListener(
                getResources().getDimensionPixelSize(R.dimen.list_item_height) * 2 / 3,
                animationDuration);
        moveItemDragListener.setOnItemMovedListener(this);
        mPositionsList.setOnDragListener(moveItemDragListener);

        new ItemTouchHelper(new GroupSwipeController(this, mPositionsList, this))
            .attachToRecyclerView(mPositionsList);

        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(this);

        // Kick-off the task to initialize list adapter
        new LoadPortfolioTask(
            this,
            getDataStore(),
            this,
            this,
            this,
            new Callback<PositionsListAdapter>() {
                @Override
                public void run(PositionsListAdapter adapter) {
                    mAdapter = adapter;
                    mPositionsList.setAdapter(adapter);
                }
            }).execute();
    }

    /**
     * Updates activity state with new intent.
     * @param intent New intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != getIntent()) {
            setIntent(intent);

            if (intent != null &&
                    intent.getBooleanExtra(ActivityNavigationConstants.FORCE_REFRESH_KEY, false)) {
                onSymbolsDataChanged();
            }
        }
    }

    /**
     * Refreshes portfolio data.
     */
    @Override
    public void onRefresh() {
        new RefreshPortfolioSymbolsDataTask(
            this,
            this,
            getDataStore(),
            mAdapter
        ).execute();
    }

    /**
     * Reloads portfolio data from persistent store and updates the view.
     */
    @Override
    void onSymbolsDataChanged() {
        if (mAdapter != null) {
            new RefreshPositionsListTask(this, getDataStore(), mAdapter)
                .execute();
        }
    }

    /**
     * Starts drag-and-drop for position list item.
     * @param item Positions list item view-model.
     * @param v View that was clicked.
     * @return True if long click was handled, otherwise false.
     */
    public boolean onPositionsListItemLongClick(@NonNull PortfolioItemViewModel item, View v) {
        return
            ViewCompat.startDragAndDrop(
                v,
                ClipData.newPlainText(item.toString(), item.toString()),
                new View.DragShadowBuilder(v),
                item,
                0);
    }

    /**
     * Toggles group expansion state.
     * @param viewHolder View holder for the group item that was clicked.
     */
    @Override
    public void onGroupClick(@NonNull final PositionsListAdapter.GroupViewHolder viewHolder) {
        final GroupViewModel item = viewHolder.getBoundData();
        final boolean newExpandedState = !item.isExpanded;

        // Updating one cell in a table should be fairly fast,
        // so we just fire and forget the task for database update
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                getDataStore().groupDao().setGroupExpandedState(item.id, newExpandedState);
            }
        });

        mAdapter.setGroupState(viewHolder, newExpandedState);
    }

    /**
     * Starts lots list activity for the selected position.
     * @param viewHolder View holder for the position item that was clicked.
     */
    @Override
    public void onPositionClick(@NonNull PositionsListAdapter.PositionViewHolder viewHolder) {
        PositionViewModel item = viewHolder.getBoundData();

        startActivity(
            new Intent(this, LotsListActivity.class)
                .putExtra(ActivityNavigationConstants.POSITION_ID_KEY, item.id)
                .putExtra(ActivityNavigationConstants.STOCK_SYMBOL_KEY, item.symbol));
    }

    /**
     * Adds new group.
     * @param parentGroup Parent group.
     * @param groupName New group name.
     */
    @Override
    public void addGroup(@NonNull GroupViewModel parentGroup, @NonNull String groupName) {
        new AddGroupTask(
            this,
            getDataStore(),
            mAdapter,
            parentGroup,
            groupName).execute();
    }

    /**
     * Adds new position.
     * @param parentGroup Parent group.
     * @param symbol Stock symbol for the position.
     */
    @Override
    public void addPosition(@NonNull GroupViewModel parentGroup, @NonNull String symbol) {
        new AddPositionTask(
            this,
            this,
            getDataStore(),
            mAdapter,
            parentGroup,
            symbol).execute();
    }

    /**
     * Renames existing group.
     * @param group Group to rename.
     * @param newName New name for the group.
     */
    @Override
    public void renameGroup(@NonNull GroupViewModel group, @NonNull String newName) {
        new RenameGroupTask(
            this,
            this,
            getDataStore().groupDao(),
            mAdapter,
            group,
            newName).execute();
    }

    /**
     * Moves portfolio item to new parent.
     * @param item Item to move.
     * @param targetGroup New parent group for the item.
     */
    @Override
    public void onItemMoved(PositionsListItemViewModel item, GroupViewModel targetGroup) {
        new MovePositionsListItemTask(
            this,
            this,
            getDataStore(),
            mAdapter,
            item,
            targetGroup).execute();
    }

    /**
     * Deletes portfolio item.
     * @param item Item to delete.
     */
    @Override
    public void onItemDeleted(PositionsListItemViewModel item) {
        new DeletePositionsListItemTask(
            this,
            this,
            getDataStore(),
            mAdapter,
            item).execute();
    }

    /**
     * Handles start of the async operation.
     */
    @Override
    public void progressStart() {
        mSwipeRefresh.setRefreshing(true);
    }

    /**
     * Handles completion of the async operation.
     */
    @Override
    public void progressEnd() {
        mSwipeRefresh.setRefreshing(false);
    }

    /**
     * Handles errors during async operation.
     * @param error Exception from async operation.
     */
    @Override
    public void progressError(Exception error) {
        Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG)
            .show();
    }
}
