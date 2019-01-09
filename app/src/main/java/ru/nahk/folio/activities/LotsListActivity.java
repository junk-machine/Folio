package ru.nahk.folio.activities;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import java.math.BigDecimal;

import ru.nahk.folio.R;
import ru.nahk.folio.model.LotEntity;
import ru.nahk.folio.model.LotViewModel;
import ru.nahk.folio.model.PortfolioDatabase;
import ru.nahk.folio.tasks.DeleteLotItemTask;
import ru.nahk.folio.tasks.LoadLotsTask;
import ru.nahk.folio.tasks.RefreshLotsListTask;
import ru.nahk.folio.tasks.RefreshLotsSymbolDataTask;
import ru.nahk.folio.tasks.SaveLotTask;
import ru.nahk.folio.utils.BigDecimalHelper;
import ru.nahk.folio.utils.CalendarHelper;
import ru.nahk.folio.utils.Callback;
import ru.nahk.folio.utils.ProgressHandler;

/**
 * Activity that displays list of lots for the portfolio position.
 */
public class LotsListActivity
    extends
        SymbolsDataTrackingActivity
    implements
        ProgressHandler,
        SwipeRefreshLayout.OnRefreshListener,
        LotsListAdapter.OnLotItemClickListener,
        LotsListAdapter.OnLotItemLongClickListener,
        DeleteItemDragListener.OnItemDeletedListener<LotViewModel> {

    /**
     * Duration for all animations.
     */
    private int mAnimationDuration;

    /**
     * Layout control that enables swipe-to-refresh functionality.
     */
    private SwipeRefreshLayout mSwipeRefresh;

    /**
     * Button to add new position lot.
     */
    private FloatingActionButton mAddLotButton;

    /**
     * Text control that is displayed when lots list is empty.
     */
    private TextView mEmptyMessage;

    /**
     * List control that displays position lots.
     */
    private RecyclerView mLotsList;

    /**
     * Adapter that provides data for the lots list view.
     */
    private LotsListAdapter mAdapter;

    /**
     * Identifier of the parent stock position.
     */
    private long mPositionId;

    /**
     * Stock symbol to display lots for.
     */
    private String mStockSymbol;

    /**
     * Flag that indicates a change in position lots.
     */
    private boolean mLotsChanged;

    /**
     * Initializes activity view.
     * @param savedInstanceState Saved activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lots_list);

        mAnimationDuration =
            getResources().getInteger(android.R.integer.config_shortAnimTime);

        if (!initializeState(savedInstanceState)
                && (getIntent() == null || !initializeState(getIntent().getExtras()))) {
            finish();
            return;
        }

        mEmptyMessage = findViewById(R.id.no_lots_message);

        // Setup delete lot button
        findViewById(R.id.button_delete)
            .setOnDragListener(new DeleteItemDragListener<>(this, mAnimationDuration));

        // Setup add lot button
        mAddLotButton = findViewById(R.id.button_add);
        mAddLotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                    new Intent(LotsListActivity.this, EditLotActivity.class)
                        .putExtra(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId)
                        .putExtra(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol)
                        .putExtra(ActivityNavigationConstants.HAS_CHANGED_KEY, mLotsChanged),
                    EditLotActivity.DEFAULT_REQUEST_CODE);
            }
        });

        // Initialize RecyclerView
        mLotsList = findViewById(R.id.lots_list_view);
        mLotsList.setHasFixedSize(true);
        mLotsList.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(this);

        // Kick-off the task to initialize list adapter
        new LoadLotsTask(
            this,
            getDataStore().lotDao(),
            mPositionId,
            this,
            this,
            new Callback<LotsListAdapter>() {
                @Override
                public void run(final LotsListAdapter adapter) {
                    mAdapter = adapter;
                    mLotsList.setAdapter(adapter);
                }
            }).execute();
    }

    /**
     * Inflates activity options menu.
     * @param menu Menu to inflate.
     * @return Always true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lots_list_options_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    /**
     * Handles menu item selection.
     * @param item Selected menu item.
     * @return True if menu item selection was handled, otherwise false.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.symbol_details:
                startActivity(
                    new Intent(this, SymbolDetailsActivity.class)
                        .putExtra(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId)
                        .putExtra(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol)
                        .putExtra(ActivityNavigationConstants.HAS_CHANGED_KEY, mLotsChanged));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Navigates back to the parent activity.
     * @param upIntent Navigation intent.
     * @return True if parent activity was on the back stack, otherwise false.
     */
    @Override
    public boolean navigateUpTo(Intent upIntent) {
        boolean result = super.navigateUpTo(upIntent);

        if (!result) {
            // When this activity is launched from the widget - back stack will be empty.
            // In this case navigation will return back to home screen.
            // Instead of that, we want to go back to the main activity,
            // therefore we need to recreate the stack with parent activity.
            TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(upIntent)
                .startActivities();
        }

        return result;
    }

    /**
     * Handles result of the add lot activity.
     * @param requestCode Identifier of the returning activity.
     * @param resultCode Activity result code.
     * @param data Results data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EditLotActivity.DEFAULT_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                if (!initializeState(data.getExtras())) {
                    finish();
                    return;
                }

                mLotsChanged = true;

                LotEntity lotEntity =
                    new LotEntity(
                        mPositionId,
                        data.getIntExtra(ActivityNavigationConstants.STOCK_QUANTITY_KEY, 0),
                        new BigDecimal(
                            data.getStringExtra(ActivityNavigationConstants.STOCK_PURCHASE_PRICE_KEY)),
                        CalendarHelper.parse(
                            data.getStringExtra(ActivityNavigationConstants.STOCK_PURCHASE_DATE_KEY)),
                        new BigDecimal(
                            data.getStringExtra(ActivityNavigationConstants.STOCK_PURCHASE_COMMISSION_KEY)));

                lotEntity.id =
                    data.getLongExtra(
                        ActivityNavigationConstants.LOT_ID_KEY,
                        PortfolioDatabase.NEW_ENTITY_ID);

                new SaveLotTask(this, this, getDataStore().lotDao(), mAdapter, lotEntity)
                    .execute();
            }
        }
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

            long previousPositionId = mPositionId;

            if (intent == null || !initializeState(intent.getExtras())) {
                finish();
                return;
            }

            if (intent.getBooleanExtra(ActivityNavigationConstants.FORCE_REFRESH_KEY, false)
                    || previousPositionId != mPositionId) {
                onSymbolsDataChanged();
            }
        }
    }

    /**
     * Saves the activity state.
     * @param outState Bundle to save the state to.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId);
        outState.putString(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol);
        outState.putBoolean(ActivityNavigationConstants.HAS_CHANGED_KEY, mLotsChanged);
    }

    /**
     * Initializes activity from the given state bundle.
     * This can be intent's extra or previously saved state.
     * @param state State bundle.
     * @return True if intent had all required data, otherwise false.
     */
    private boolean initializeState(Bundle state) {
        if (state != null) {
            mPositionId = state.getLong(ActivityNavigationConstants.POSITION_ID_KEY, -1);
            mStockSymbol = state.getString(ActivityNavigationConstants.STOCK_SYMBOL_KEY);
            mLotsChanged = state.getBoolean(ActivityNavigationConstants.HAS_CHANGED_KEY, false);

            if (mPositionId != -1 && !TextUtils.isEmpty(mStockSymbol)) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(
                        getString(R.string.lots_list_title_format, mStockSymbol));
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Creates the intent to return to parent activity.
     * @return Intent to return to parent activity.
     */
    @Override
    public Intent getParentActivityIntent() {
        Intent parentIntent = super.getParentActivityIntent();

        if (parentIntent != null && mLotsChanged) {
            parentIntent.putExtra(ActivityNavigationConstants.FORCE_REFRESH_KEY, true);
        }

        return parentIntent;
    }

    /**
     * Starts edit lot activity for the selected position lot.
     * @param item Selected position lot item.
     */
    @Override
    public void onLotClick(LotViewModel item) {
        startActivityForResult(
            new Intent(LotsListActivity.this, EditLotActivity.class)
                .putExtra(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId)
                .putExtra(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol)
                .putExtra(ActivityNavigationConstants.HAS_CHANGED_KEY, mLotsChanged)
                // Information about existing lot
                .putExtra(ActivityNavigationConstants.LOT_ID_KEY, item.id)
                .putExtra(
                    ActivityNavigationConstants.STOCK_QUANTITY_KEY,
                    item.quantity)
                .putExtra(
                    ActivityNavigationConstants.STOCK_PURCHASE_PRICE_KEY,
                    BigDecimalHelper.stripTrailingZeros(item.purchasePrice).toPlainString())
                .putExtra(
                    ActivityNavigationConstants.STOCK_PURCHASE_DATE_KEY,
                    CalendarHelper.toString(item.purchaseDate))
                .putExtra(
                    ActivityNavigationConstants.STOCK_PURCHASE_COMMISSION_KEY,
                    BigDecimalHelper.stripTrailingZeros(item.commission).toPlainString()),
            EditLotActivity.DEFAULT_REQUEST_CODE);
    }

    /**
     * Starts drag-and-drop for position lot item.
     * @param item Position lot item view-model.
     * @param v View that was clicked.
     * @return True if long click was handled, otherwise false.
     */
    @Override
    public boolean onLotItemLongClick(@NonNull LotViewModel item, View v) {
        return
            ViewCompat.startDragAndDrop(
                v,
                ClipData.newPlainText(item.toString(), item.toString()),
                new View.DragShadowBuilder(v),
                item,
                0);
    }

    /**
     * Deletes portfolio position lot item.
     * @param item Information about the lot item to remove.
     */
    @Override
    public void onItemDeleted(final LotViewModel item) {
        mLotsChanged = true;

        new DeleteLotItemTask(
            this,
            this,
            getDataStore().lotDao(),
            mAdapter,
            item,
            new Callback<Integer>() {
                @Override
                public void run(Integer arg) {
                    if (arg != null) {
                        mAdapter.notifyItemRemoved(arg);
                    }

                    updateEmptyMessageVisibility();
                }
        }).execute();
    }

    /**
     * Refreshes lots list.
     */
    @Override
    public void onRefresh() {
        mLotsChanged = true;

        new RefreshLotsSymbolDataTask(
            this,
            this,
            mStockSymbol,
            mPositionId,
            getDataStore(),
            mAdapter).execute();
    }

    /**
     * Reloads lots data from persistent store and updates the view.
     */
    @Override
    void onSymbolsDataChanged() {
        if (mAdapter != null) {
            new RefreshLotsListTask(this, getDataStore().lotDao(), mAdapter, mPositionId)
                .execute();
        }
    }

    /**
     * Handles start of the async operation.
     */
    @Override
    public void progressStart() {
        mSwipeRefresh.setRefreshing(true);

        mAddLotButton.animate()
            .setInterpolator(new LinearInterpolator())
            .scaleX(0)
            .scaleY(0)
            .setDuration(mAnimationDuration)
            .start();
    }

    /**
     * Handles completion of the async operation.
     */
    @Override
    public void progressEnd() {
        mAddLotButton.animate()
            .setInterpolator(new OvershootInterpolator())
            .scaleX(1)
            .scaleY(1)
            .setDuration(mAnimationDuration)
            .start();

        updateEmptyMessageVisibility();
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

    /**
     * Updates visibility state of empty list message.
     */
    private void updateEmptyMessageVisibility() {
        if (mAdapter.getItemCount() == 0) {
            mEmptyMessage.setVisibility(View.VISIBLE);
            mEmptyMessage.animate()
                .alpha(1)
                .setDuration(mAnimationDuration)
                .start();
        } else {
            mEmptyMessage.animate()
                .alpha(0)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mEmptyMessage.setVisibility(View.GONE);
                    }
                })
                .setDuration(mAnimationDuration)
                .start();
        }
    }
}
