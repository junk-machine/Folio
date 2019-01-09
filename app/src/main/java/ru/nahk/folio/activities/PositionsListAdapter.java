package ru.nahk.folio.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;

import ru.nahk.folio.R;
import ru.nahk.folio.controls.LevelStripes;
import ru.nahk.folio.model.GroupViewModel;
import ru.nahk.folio.model.PortfolioItemViewModel;
import ru.nahk.folio.model.PositionViewModel;
import ru.nahk.folio.model.PositionsListItemViewModel;
import ru.nahk.folio.utils.BigDecimalHelper;
import ru.nahk.folio.utils.DynamicArray;
import ru.nahk.folio.utils.ThemedResources;

/**
 * Adapter for {@link RecyclerView} that maintains portfolio tree view.
 */
public class PositionsListAdapter
    extends RecyclerView.Adapter<PositionsListAdapter.PositionsListItemViewHolder> {

    /**
     * Event listener for positions list item long clicks.
     */
    public interface OnItemLongClickListener {
        /**
         * Handles positions list item long click.
         * @param item View-model for the positions list item that is clicked.
         * @param v View that was clicked.
         * @return True if long click was handled, otherwise false.
         */
        boolean onPositionsListItemLongClick(@NonNull PortfolioItemViewModel item, View v);
    }

    /**
     * Event listener for group item clicks.
     */
    public interface OnGroupItemClickListener {
        /**
         * Callback to run when group item is clicked.
         * @param viewHolder View holder for the group item that was clicked.
         */
        void onGroupClick(@NonNull GroupViewHolder viewHolder);
    }

    /**
     * Event listener for position item clicks.
     */
    public interface OnPositionItemClickListener {
        /**
         * Callback to run when position item is clicked.
         * @param viewHolder View holder for the position item that was clicked.
         */
        void onPositionClick(@NonNull PositionViewHolder viewHolder);
    }

    /**
     * Base class for any positions list item view holder.
     */
    abstract class PositionsListItemViewHolder<TData extends PositionsListItemViewModel>
        extends RecyclerView.ViewHolder
        implements View.OnLongClickListener {

        /**
         * Data that is currently bound.
         */
        private TData mData;

        /**
         * Flag that indicates whether extended information should be displayed.
         */
        boolean mShowExtendedInformation;

        /**
         * Control to display position level stripes.
         */
        private LevelStripes mLevelStripesView;

        /**
         * Control to display the name of the item.
         */
        TextView mNameView;

        /**
         * Control to display current stocks value.
         */
        TextView mCurrentValueView;

        /**
         * Control to display change in the stock value.
         */
        private TextView mValueChangeView;

        /**
         * Listener for item's long click events.
         */
        private OnItemLongClickListener mItemLongClickListener;

        /**
         * Creates new view holder for any positions list item view.
         * @param itemView Control to display positions list item.
         */
        PositionsListItemViewHolder(View itemView) {
            super(itemView);

            mLevelStripesView = itemView.findViewById(R.id.position_level_stripes);
            mNameView = itemView.findViewById(R.id.position_name);
            mCurrentValueView = itemView.findViewById(R.id.position_current_value);
            mValueChangeView = itemView.findViewById(R.id.position_value_change);

            itemView.setOnLongClickListener(this);
        }

        /**
         * Gets currently bound data.
         * @return Data that is currently bound.
         */
        TData getBoundData() {
            return mData;
        }

        /**
         * Handles list item long clicks.
         * @param v List item view.
         * @return True if long click was handled, otherwise false.
         */
        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null) {
                return mItemLongClickListener.onPositionsListItemLongClick(mData, v);
            }

            return false;
        }

        /**
         * Updates view with the given data.
         * @param item Information about positions list item.
         * @param onLongClickListener Listener for the list item long click events.
         */
        void bind(
            @NonNull final TData item,
            final OnItemLongClickListener onLongClickListener) {

            mData = item;
            mItemLongClickListener = onLongClickListener;

            mShowExtendedInformation = false;

            mLevelStripesView.setPositionsListItem(item);
            mNameView.setText(getItemDisplayName(item));

            int valueChangeDirection = 0;

            if (item.currentValue == null) {
                mCurrentValueView.setText(getEmptyCurrentValueText());
                mValueChangeView.setText(getEmptyValueChangeText());
            } else {
                mCurrentValueView.setText(
                    BigDecimalHelper.formatCurrency(item.currentValue));

                mShowExtendedInformation =
                    PositionsListAdapter.this.mRecyclerView != null
                        && (PositionsListAdapter.this.mRecyclerView.getWidth() /
                            itemView.getResources().getDimension(R.dimen.list_item_height)) > EXTENDED_INFO_RATIO;

                if (item.baseValue == null) {
                    mValueChangeView.setText(getEmptyValueChangeText());
                } else {
                    valueChangeDirection =
                        item.currentValue.compareTo(item.baseValue);

                    BigDecimal valueChange = item.currentValue.subtract(item.baseValue);

                    mValueChangeView.setText(
                        String.format(
                            itemView.getContext().getString(R.string.extended_value_change_format),
                            BigDecimalHelper.formatCurrencyChange(valueChange),
                            mShowExtendedInformation
                                ? BigDecimalHelper.formatPercentage(item.baseValue, valueChange)
                                : BigDecimalHelper.formatPercentageShort(item.baseValue, valueChange)));
                }
            }

            mValueChangeView.setTextAppearance(
                valueChangeDirection > 0
                    ? R.style.TextAppearance_ListItem_Value_Positive
                    : (valueChangeDirection < 0
                        ? R.style.TextAppearance_ListItem_Value_Negative
                        : R.style.TextAppearance_ListItem_Value_NoChange));
        }

        /**
         * Retrieves display name of the item.
         * @param item Current item.
         * @return Display name of the item.
         */
        String getItemDisplayName(TData item) {
            return item.name;
        }

        /**
         * Retrieves text to display when current value is NULL.
         * @return Text to display when current value is NULL.
         */
        String getEmptyCurrentValueText() {
            return null;
        }

        /**
         * Retrieves text to display when value change is NULL.
         * @return Text to display when value change is NULL.
         */
        String getEmptyValueChangeText() {
            return null;
        }
    }

    /**
     * View holder for position items group view.
     */
    class GroupViewHolder
        extends PositionsListItemViewHolder<GroupViewModel>
        implements View.OnClickListener {

        /**
         * Duration for all animations.
         */
        private final int mAnimationDuration;

        /**
         * Control that displays current group expansion state.
         */
        private final ImageView mGroupStateIcon;

        /**
         * Listener for item click events.
         */
        private OnGroupItemClickListener mItemClickListener;

        /**
         * Creates new view holder for position items group view.
         * @param itemView Control to display items group.
         */
        GroupViewHolder(View itemView) {
            super(itemView);

            mAnimationDuration = itemView.getResources().getInteger(android.R.integer.config_shortAnimTime);
            mGroupStateIcon = itemView.findViewById(R.id.group_state_icon);

            itemView.setOnClickListener(this);
        }

        /**
         * Handles click events on the associated view by toggling group state.
         * @param v Group item view.
         */
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onGroupClick(this);
            }
        }

        /**
         * Populates the view control with group data.
         * @param item Details about portfolio position items group.
         * @param onLongClickListener Listener for the group item long click events.
         * @param onClickListener Listener for the group item click events.
         */
        void bind(
            @NonNull final GroupViewModel item,
            final OnItemLongClickListener onLongClickListener,
            final OnGroupItemClickListener onClickListener) {

            super.bind(item, onLongClickListener);

            mGroupStateIcon.setRotation(getStateIconRotation(item.isExpanded));
            setViewStyles(item.isExpanded);

            mItemClickListener = onClickListener;
        }

        /**
         * Updates view to depict group expansion state.
         * @param isExpanded Flag that indicates whether group is expanded or not.
         */
        void setVisualGroupState(final boolean isExpanded) {
            mGroupStateIcon.animate()
                .rotation(getStateIconRotation(isExpanded))
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(mAnimationDuration )
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setViewStyles(isExpanded);
                    }
                })
                .start();
        }

        /**
         * Updates styles of the view based on expansion state.
         * @param isExpanded Flag that indicates whether group is expanded or not.
         */
        private void setViewStyles(boolean isExpanded) {
            mNameView.setTextAppearance(
                isExpanded
                    ? R.style.TextAppearance_ListItem_ItemTitle
                    : R.style.TextAppearance_ListItem_ItemTitle_Collapsed);

            ImageViewCompat.setImageTintList(
                mGroupStateIcon, ColorStateList.valueOf(mNameView.getCurrentTextColor()));

            mCurrentValueView.setTextAppearance(
                isExpanded
                    ? R.style.TextAppearance_ListItem_Value
                    : R.style.TextAppearance_ListItem_Value_Collapsed
            );
        }

        /**
         * Gets the rotation of the group state icon based on current state and RTL configuration.
         * @return Rotation angle of the group state icon.
         */
        private int getStateIconRotation(boolean isExpanded) {
            if (isExpanded) return 0;

            return
                ViewCompat.getLayoutDirection(itemView) == ViewCompat.LAYOUT_DIRECTION_RTL
                    ? 90 : -90;
        }
    }

    /**
     * View holder for the portfolio position item.
     */
    public class PositionViewHolder
        extends PositionsListItemViewHolder<PositionViewModel>
        implements View.OnClickListener {

        /**
         * Control to display the stock symbol.
         */
        private final TextView mSymbolView;

        /**
         * Control to display total number of shares for the position.
         */
        private final TextView mQuantityView;

        /**
         * Control that displays an icon for symbol value change.
         */
        private final ImageView mSymbolValueChangeIcon;

        /**
         * Listener for item click events.
         */
        private OnPositionItemClickListener mItemClickListener;

        /**
         * Creates new view holder for portfolio position view.
         * @param itemView Control to display empty portfolio position.
         */
        PositionViewHolder(View itemView) {
            super(itemView);

            mSymbolView = itemView.findViewById(R.id.position_symbol);
            mQuantityView = itemView.findViewById(R.id.position_quantity);
            mSymbolValueChangeIcon = itemView.findViewById(R.id.symbol_value_change_icon);

            itemView.setOnClickListener(this);
        }

        /**
         * Handles click events on the associated view by passing click event to external listener.
         * @param v Position item view.
         */
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onPositionClick(this);
            }
        }

        /**
         * Populates the view control with portfolio position data.
         * @param item Details about portfolio position.
         * @param onLongClickListener Listener for the position item long click events.
         * @param onClickListener Listener for the position item click events.
         */
        void bind(
            @NonNull final PositionViewModel item,
            final OnItemLongClickListener onLongClickListener,
            final OnPositionItemClickListener onClickListener) {

            super.bind(item, onLongClickListener);

            if (item.quantity > 0) {
                int symbolValueChangeDirection = 0;
                if (item.symbolValueChange != null) {
                    symbolValueChangeDirection =
                        item.symbolValueChange.compareTo(BigDecimal.ZERO);
                }

                if (mShowExtendedInformation) {
                    setExtendedSymbolValueText(mSymbolView, item, symbolValueChangeDirection);

                    mQuantityView.setVisibility(View.VISIBLE);

                    mQuantityView.setText(
                        itemView.getResources().getQuantityString(
                            R.plurals.position_shares_count_format,
                            item.quantity,
                            item.quantity));

                    mSymbolValueChangeIcon.setVisibility(View.GONE);
                } else {
                    mSymbolView.setText(item.symbol);

                    mQuantityView.setVisibility(View.GONE);

                    // If there are lots for the position and no extended information is shown,
                    // display symbol change icon, otherwise symbol value change will be displayed
                    // as text, either for the whole position or as part of symbol name
                    mSymbolValueChangeIcon.setVisibility(View.VISIBLE);
                    mSymbolValueChangeIcon.setImageResource(
                        symbolValueChangeDirection > 0
                            ? R.drawable.arrow_up
                            : (symbolValueChangeDirection < 0
                                ? R.drawable.arrow_down
                                : R.drawable.circle));
                }
            } else {
                mSymbolView.setText(item.symbol);

                mQuantityView.setVisibility(View.GONE);
                mSymbolValueChangeIcon.setVisibility(View.GONE);
            }

            mItemClickListener = onClickListener;
        }

        /**
         * Sets text for symbol view that includes the price and value change.
         * @param symbolView Text view to display symbol information.
         * @param item Item that holds symbol information.
         * @param symbolValueChangeDirection Sign of the symbol value change.
         */
        private void setExtendedSymbolValueText(
            TextView symbolView,
            PositionViewModel item,
            int symbolValueChangeDirection) {

            symbolView.setText(item.symbol);

            // Append current symbol value
            if (item.symbolValue != null) {
                symbolView.append(
                    itemView.getResources().getString(R.string.extended_symbol_format_splitter));

                Spannable symbolValue =
                    new SpannableString(BigDecimalHelper.formatCurrency(item.symbolValue));
                symbolValue.setSpan(
                    new ForegroundColorSpan(
                        ThemedResources.getColor(
                            itemView.getContext(),
                            android.R.attr.textColorPrimary,
                            Color.WHITE)),
                    0,
                    symbolValue.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                symbolView.append(symbolValue);
            }

            // Append current symbol value change
            if (item.symbolValueChange != null) {
                symbolView.append(
                    itemView.getResources().getString(R.string.extended_symbol_format_splitter));

                Spannable symbolValueChange =
                    new SpannableString(
                        itemView.getResources().getString(
                            R.string.extended_value_change_format,
                            BigDecimalHelper.formatCurrencyChange(item.symbolValueChange),
                            BigDecimalHelper.formatPercentage(
                                item.symbolValue.add(item.symbolValueChange),
                                item.symbolValueChange)));

                symbolValueChange.setSpan(
                    new ForegroundColorSpan(
                        ContextCompat.getColor(
                            itemView.getContext(),
                            symbolValueChangeDirection > 0
                                ? R.color.positiveChange
                                : (symbolValueChangeDirection < 0
                                    ? R.color.negativeChange
                                    : R.color.noChange))),
                    0,
                    symbolValueChange.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                symbolView.append(symbolValueChange);
            }
        }

        /**
         * Retrieves display name for the item.
         * @param item Current item.
         * @return Company name or stock symbol, if name is NULL.
         */
        @Override
        protected String getItemDisplayName(PositionViewModel item) {
            return item.name == null
                ? item.symbol
                : super.getItemDisplayName(item);
        }

        /**
         * Retrieves text to display when current value is NULL.
         * @return Text to display when current value is NULL.
         */
        @Override
        protected String getEmptyCurrentValueText() {
            return itemView.getResources().getString(R.string.unknown_value);
        }
    }

    /**
     * Minimal list item size ratio when extended information should be displayed.
     * Height is fixed, so we can show/hide additional information based on width-to-height ratio.
     */
    private static final int EXTENDED_INFO_RATIO = 8;

    /**
     * Type of the view for a group item.
     */
    private static final int VIEW_TYPE_GROUP = 0;

    /**
     * Type of the view for a portfolio position.
     */
    private static final int VIEW_TYPE_POSITION = 1;

    /**
     * Parent {@link RecyclerView} instance.
     */
    private RecyclerView mRecyclerView;

    /**
     * Flat list of visible portfolio items.
     */
    private DynamicArray<PositionsListItemViewModel> mVisibleItems;

    /**
     * Listener for item long click events.
     */
    private OnItemLongClickListener mItemLongClickListener;

    /**
     * Listener for positions group click events.
     */
    private OnGroupItemClickListener mGroupItemClickListener;

    /**
     * Listener for position item click events.
     */
    private OnPositionItemClickListener mPositionItemClickListener;

    /**
     * Creates new instance of the {@link PositionsListAdapter} class.
     */
    public PositionsListAdapter() {
        mVisibleItems = new DynamicArray<>();
    }

    /**
     * Updates parent {@link RecyclerView} reference.
     * @param recyclerView New parent {@link RecyclerView}.
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    /**
     * Retrieves view type for the list item at the given index.
     * @param position Index of the list item.
     * @return Type of the view to display given list item.
     */
    @Override
    public int getItemViewType(int position) {
        PositionsListItemViewModel item = mVisibleItems.get(position);

        if (item instanceof GroupViewModel) {
            return VIEW_TYPE_GROUP;
        } else if (item instanceof PositionViewModel) {
            return VIEW_TYPE_POSITION;
        }

        // TODO: Error view?
        return VIEW_TYPE_POSITION;
    }

    /**
     * Creates new list item view holder, which hosts the view
     * that displays information about positions list item.
     * @param parent View parent.
     * @param viewType Type of the view to create.
     * @return View holder for the positions list item.
     */
    @Override
    @NonNull
    public PositionsListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_GROUP:
                return new GroupViewHolder(
                    LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.group_item, parent, false));
            case VIEW_TYPE_POSITION:
            default:
                return new PositionViewHolder(
                    LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.position_item, parent, false));
        }
    }

    /**
     * Populates portfolio position view with the information about specific position.
     * @param holder Portfolio position view holder.
     * @param position Index of the desired portfolio position.
     */
    @Override
    public void onBindViewHolder(@NonNull final PositionsListItemViewHolder holder, int position) {
        if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder)holder).bind(
                (GroupViewModel) mVisibleItems.get(position),
                mItemLongClickListener,
                mGroupItemClickListener);
        }
        else if (holder instanceof PositionViewHolder) {
            ((PositionViewHolder)holder).bind(
                (PositionViewModel) mVisibleItems.get(position),
                mItemLongClickListener,
                mPositionItemClickListener);
        }
    }

    /**
     * Retrieves the total number of visible items in the list.
     * These are items in all expanded groups.
     * @return Total number of visible items in the list.
     */
    @Override
    public int getItemCount() {
        return mVisibleItems.size();
    }

    /**
     * Sets the long click listener for positions list items.
     * @param listener Long click listener for positions list items.
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    /**
     * Sets the click listener for positions group items.
     * @param listener On-click listener for positions group items.
     */
    public void setOnGroupClickListener(OnGroupItemClickListener listener) {
        mGroupItemClickListener = listener;
    }

    /**
     * Sets the click listener for portfolio position items.
     * @param listener On-click listener for portfolio position items.
     */
    public void setOnPositionClickListener(OnPositionItemClickListener listener) {
        mPositionItemClickListener = listener;
    }

    /**
     * Expands or collapses positions group.
     * @param viewHolder View holder for the group item to change.
     * @param newState New group expansion state.
     */
    void setGroupState(@NonNull GroupViewHolder viewHolder, boolean newState) {
        viewHolder.setVisualGroupState(newState);

        GroupViewModel item = viewHolder.getBoundData();
        int itemIndex = viewHolder.getAdapterPosition();

        if (newState) {
            // Expand group: add visible children to the flat list
            item.isExpanded = true;
            DynamicArray<PositionsListItemViewModel> groupChildren = new DynamicArray<>();
            addVisibleChildren(item, groupChildren);
            mVisibleItems.addRange(itemIndex + 1, groupChildren);
            notifyItemRangeInserted(itemIndex + 1, groupChildren.size());
        } else {
            // Collapse group: remove visible children from the flat list
            int visibleChildrenCount = countVisibleChildren(item);
            mVisibleItems.removeRange(itemIndex + 1, visibleChildrenCount);
            item.isExpanded = false;
            notifyItemRangeRemoved(itemIndex + 1, visibleChildrenCount);
        }

        // Repaint group item itself
        notifyItemChanged(itemIndex);
    }

    /**
     * Retrieves display index of the visible item or NULL, if item is not visible.
     * @param item Item to look up.
     * @return Item index or NULL.
     */
    public Integer getItemIndex(PositionsListItemViewModel item) {
        for (int itemIndex = 0; itemIndex < mVisibleItems.size(); ++itemIndex){
            if (mVisibleItems.get(itemIndex) == item) {
                return itemIndex;
            }
        }

        return null;
    }

    /**
     * Scrolls parent list view of the visible item with given identifier.
     * @param itemId Identifier of the item to scroll to.
     */
    public void scrollToItem(long itemId) {
        if (mRecyclerView != null) {
            for (int itemIndex = 0; itemIndex < mVisibleItems.size(); ++itemIndex) {
                if (mVisibleItems.get(itemIndex).id == itemId) {
                    mRecyclerView.smoothScrollToPosition(itemIndex);
                    return;
                }
            }
        }
    }

    /**
     * Sets the underlying list items buffer to the given root positions group.
     * If this method is called after initial binding, then {@code notifyDataSetChanged()}
     * has to be called explicitly from the UI thread.
     */
    public void setData(GroupViewModel root) {
        mVisibleItems.clear();
        mVisibleItems.add(root);

        addVisibleChildren(root, mVisibleItems);
    }

    /**
     * Adds all visible children of the given group item to the target collection.
     * @param item Group item to process children for.
     * @param target Collection that accumulates all visible children.
     */
    private void addVisibleChildren(GroupViewModel item, DynamicArray<PositionsListItemViewModel> target) {
        if (item.isExpanded) {
            for (int childIndex = 0; childIndex < item.children.size(); ++childIndex) {
                PositionsListItemViewModel child = item.children.get(childIndex);
                target.add(child);

                if (child instanceof GroupViewModel) {
                    addVisibleChildren((GroupViewModel) child, target);
                }
            }
        }
    }

    /**
     * Counts child items in all expanded groups.
     * @param item Group item to count children for.
     * @return Number of child items in all expanded groups.
     */
    private int countVisibleChildren(GroupViewModel item) {
        int result = 0;

        if (item.isExpanded) {
            for (int childIndex = 0; childIndex < item.children.size(); ++childIndex) {
                ++result;

                PositionsListItemViewModel child = item.children.get(childIndex);

                if (child instanceof GroupViewModel) {
                    result += countVisibleChildren((GroupViewModel) child);
                }
            }
        }

        return result;
    }
}
