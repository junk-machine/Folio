package ru.nahk.folio.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import ru.nahk.folio.R;
import ru.nahk.folio.controls.TextInputAutoCompleteTextView;
import ru.nahk.folio.model.GroupDao;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.stockapi.StockApiFactory;
import ru.nahk.folio.stockapi.SymbolInfo;
import ru.nahk.folio.utils.ThemedResources;
import ru.nahk.folio.utils.WindowHelper;
import ru.nahk.folio.validators.NotEmptyTextValidator;
import ru.nahk.folio.validators.TextValidatorBase;

/**
 * Touch helper for {@link RecyclerView} that displays popup menu when group items are swiped.
 */
public class GroupSwipeController extends ItemTouchHelper.Callback {
    /**
     * Handler for successful positions group actions.
     */
    public interface GroupActionsHandler {
        /**
         * Adds new positions group.
         * @param parentGroup Parent group.
         * @param groupName New group name.
         */
        void addGroup(GroupViewModel parentGroup, String groupName);

        /**
         * Adds new portfolio position.
         * @param parentGroup Parent group.
         * @param symbol Stock symbol.
         */
        void addPosition(GroupViewModel parentGroup, String symbol);

        /**
         * Renames positions group.
         * @param group Group to rename.
         * @param newName New name.
         */
        void renameGroup(GroupViewModel group, String newName);
    }

    /**
     * Parent activity context.
     */
    private Context mContext;

    /**
     * Current handler for successful group actions.
     */
    private GroupActionsHandler mGroupActionsHandler;

    /**
     * Options icon to reveal when swiping.
     */
    private Drawable mOptionsIcon;

    /**
     * Rectangle that holds options icon coordinates.
     */
    private Rect mOptionsIconPosition;

    /**
     * Flag that indicates whether swipe was finished.
     */
    private boolean mSwipeFinished;

    /**
     * Flag that indicates whether popup menu is displayed.
     */
    private boolean mPopupShown;

    /**
     * Swipe threshold before popup menu is displayed.
     */
    private float mSwipeThreshold;

    /**
     * Creates new instance of the {@link GroupSwipeController} class
     * with the provided activity context, recycler view and group actions handler.
     * @param context Parent activity context.
     * @param recyclerView Associated recycler view.
     * @param groupActionsHandler Handler for successful group actions.
     */
    GroupSwipeController(
            @NonNull Context context,
            @NonNull RecyclerView recyclerView,
            @NonNull GroupActionsHandler groupActionsHandler) {
        mContext = context;
        mGroupActionsHandler = groupActionsHandler;

        mOptionsIcon = ContextCompat.getDrawable(mContext, R.drawable.options).mutate();
        mOptionsIcon.setTint(
            ThemedResources.getColor(context, android.R.attr.textColorPrimary, Color.WHITE));

        mOptionsIconPosition = new Rect();

        mSwipeThreshold = mOptionsIcon.getIntrinsicWidth();

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // When action is finished, will reset item back to its original position
                mSwipeFinished =
                    event.getAction() == MotionEvent.ACTION_CANCEL
                        || event.getAction() == MotionEvent.ACTION_UP;

                return false;
            }
        });
    }

    /**
     * Allows swipe left motion for group items.
     * @param recyclerView Associated recycler view.
     * @param viewHolder View holder to check supported actions for.
     * @return Supported movement actions.
     */
    @Override
    public int getMovementFlags(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof PositionsListAdapter.GroupViewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.LEFT);
        } else {
            return 0;
        }
    }

    /**
     * Does nothing.
     * @param recyclerView Associated recycler view.
     * @param viewHolder Source view holder.
     * @param viewHolder1 Target view holder.
     * @return Always false.
     */
    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    /**
     * Does nothing.
     * @param viewHolder View holder of the item that is being swiped.
     * @param i Swiped item position.
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        // We don't really care about actual swipe-out functionality.
        // Popup will be created as soon as element moves past our own swipe threshold.
    }

    /**
     * Draws options icon when view is being swiped to the left.
     * @param c View canvas.
     * @param recyclerView Associated recycler view.
     * @param viewHolder Item's view holder.
     * @param dX Horizontal item movement.
     * @param dY Vertical item movement.
     * @param actionState Type of interaction on the view.
     * @param isCurrentlyActive Flag that indicates whether user interacts with the view.
     */
    @Override
    public void onChildDraw(
        @NonNull Canvas c,
        @NonNull RecyclerView recyclerView,
        @NonNull RecyclerView.ViewHolder viewHolder,
        float dX,
        float dY,
        int actionState,
        boolean isCurrentlyActive) {

        c.getClipBounds(mOptionsIconPosition);

        if (!mPopupShown && -dX > mSwipeThreshold) {
            if (viewHolder instanceof PositionsListAdapter.GroupViewHolder) {
                mPopupShown = true;

                showPopup(
                    ((PositionsListAdapter.GroupViewHolder)viewHolder).getBoundData(),
                    viewHolder.itemView);
            }
        }

        float iconWidth = Math.min(-dX, mSwipeThreshold);
        mOptionsIconPosition.left = mOptionsIconPosition.right - Math.round(iconWidth);

        float verticalCenter = (viewHolder.itemView.getTop() + viewHolder.itemView.getBottom()) / 2;
        mOptionsIconPosition.top = Math.round(verticalCenter - (iconWidth / 2));
        mOptionsIconPosition.bottom = mOptionsIconPosition.top + Math.round(iconWidth);

        mOptionsIcon.setBounds(mOptionsIconPosition);
        mOptionsIcon.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    /**
     * Prevents the item from being swiped away.
     * @param flags Movement flags.
     * @param layoutDirection Layout direction of the recycler view.
     * @return Converted movement flags.
     */
    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (mSwipeFinished) {
            // Return item back to original position
            mSwipeFinished = false;
            return 0;
        }

        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    /**
     * Displays popup menu.
     * @param groupItem Group item to display popup menu for.
     * @param itemView Group item's view.
     */
    private void showPopup(final GroupViewModel groupItem, View itemView) {
        PopupMenu popup =
            new PopupMenu(mContext, itemView, Gravity.RIGHT, 0, R.style.PopupMenu_GroupActions);

        popup
            .getMenuInflater()
            .inflate(
                groupItem.id == GroupDao.ROOT_GROUP_ID
                    ? R.menu.root_group_popup_menu
                    : R.menu.group_popup_menu,
                popup.getMenu());

        MenuCompat.setGroupDividerEnabled(popup.getMenu(), true);

        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu popupMenu) {
                // Dismiss is called every time when popup disappears,
                // so we don't need to do this in the click listener
                mPopupShown = false;
            }
        });

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return handleMenuItemClick(groupItem, menuItem);
            }
        });

        popup.show();
    }

    /**
     * Handles selection of the menu item.
     * @param groupItem Associated group item.
     * @param menuItem Selected menu item.
     * @return True if selection was handled, otherwise false.
     */
    private boolean handleMenuItemClick(final GroupViewModel groupItem, MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        switch (itemId) {
            case R.id.add_group:
                showGroupNameDialog(
                    R.string.add_group_title,
                    null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mGroupActionsHandler != null) {
                                Dialog sender = (Dialog)dialog;
                                mGroupActionsHandler.addGroup(
                                    groupItem,
                                    ((EditText) sender.findViewById(R.id.group_name_text))
                                        .getText().toString());
                            }
                        }
                    });
                return true;

            case R.id.add_position:
                showAddPositionDialog(groupItem);
                return true;

            case R.id.rename_group:
                showGroupNameDialog(
                    R.string.rename_group_title,
                    groupItem.name,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mGroupActionsHandler != null) {
                                Dialog sender = (Dialog)dialog;
                                mGroupActionsHandler.renameGroup(
                                    groupItem,
                                    ((EditText) sender.findViewById(R.id.group_name_text))
                                        .getText().toString());
                            }
                        }
                    });
                return true;

            case R.id.about_app:
                mContext.startActivity(new Intent(mContext, AboutActivity.class));
                return true;
        }

        return false;
    }

    /**
     * Displays the dialog to edit group name.
     * @param titleResourceId String resource identifier of the dialog title.
     * @param initialGroupName Initial group name, if any.
     * @param okClickListener Listener for OK button click.
     */
    private void showGroupNameDialog(
        int titleResourceId,
        String initialGroupName,
        DialogInterface.OnClickListener okClickListener) {

        AlertDialog.Builder dialogBuilder =
            new AlertDialog.Builder(mContext)
                .setView(R.layout.edit_group_name_dialog)
                .setTitle(titleResourceId)
                .setPositiveButton(android.R.string.ok, okClickListener)
                .setNegativeButton(android.R.string.cancel, null);

        final AlertDialog dialog = dialogBuilder.create();
        WindowHelper.ensureKeyboard(mContext, dialog.getWindow());
        dialog.show();

        // Note: Below code has to happen after the call to show(), otherwise layout is not inflated yet
        TextInputEditText groupNameText = dialog.findViewById(R.id.group_name_text);

        if (groupNameText != null) {
            if (initialGroupName != null) {
                groupNameText.setText(initialGroupName);
            }

            groupNameText.setOnEditorActionListener(
                new ImeActionDoneButtonClickDispatcher(dialog.getButton(DialogInterface.BUTTON_POSITIVE)));
        }

        // Attach validator to group name text box
        new NotEmptyTextValidator(
            (TextInputLayout) dialog.findViewById(R.id.group_name_text_layout),
            R.string.empty_group_name_error
        ).setOnValidationStateChangedListener(new TextValidatorBase.OnValidationStateChangedListener() {
            @Override
            public void onValidationStateChanged(boolean isValid) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isValid);
            }
        });
    }

    /**
     * Displays the dialog to edit stock symbol for the position.
     * @param parentGroup Parent positions group.
     */
    private void showAddPositionDialog(final GroupViewModel parentGroup) {
        AlertDialog.Builder dialogBuilder =
            new AlertDialog.Builder(mContext)
                .setView(R.layout.add_position_dialog)
                .setTitle(R.string.add_position_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mGroupActionsHandler != null) {
                            Dialog sender = (Dialog) dialog;
                            mGroupActionsHandler.addPosition(
                                parentGroup,
                                ((EditText) sender.findViewById(R.id.position_symbol_text))
                                    .getText().toString().toUpperCase());
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        final AlertDialog dialog = dialogBuilder.create();
        WindowHelper.ensureKeyboard(mContext, dialog.getWindow());
        dialog.show();

        TextInputAutoCompleteTextView positionSymbolText =
            dialog.findViewById(R.id.position_symbol_text);

        if (positionSymbolText != null) {
            final Button okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            positionSymbolText.setOnEditorActionListener(
                new ImeActionDoneButtonClickDispatcher(okButton));

            positionSymbolText.setAdapter(
                new SymbolsAutoCompleteAdapter(mContext, StockApiFactory.getApi()));

            // Use item selected in the auto-complete list
            positionSymbolText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SymbolInfo selectedSymbol =
                        (SymbolInfo) ((SymbolsAutoCompleteAdapter) parent.getAdapter()).getItem(position);

                    if (selectedSymbol != null && okButton.isEnabled()) {
                        okButton.performClick();
                    }
                }
            });
        }

        // Attach validator to position stock symbol text box
        // Note: This has to happen after the call to show(), otherwise layout is not inflated yet
        new NotEmptyTextValidator(
            (TextInputLayout) dialog.findViewById(R.id.position_symbol_text_layout),
            R.string.empty_position_symbol_error
        ).setOnValidationStateChangedListener(new TextValidatorBase.OnValidationStateChangedListener() {
            @Override
            public void onValidationStateChanged(boolean isValid) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isValid);
            }
        });
    }
}
