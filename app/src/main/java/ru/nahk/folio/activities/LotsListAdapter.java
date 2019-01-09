package ru.nahk.folio.activities;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.List;

import ru.nahk.folio.R;
import ru.nahk.folio.model.LotViewModel;
import ru.nahk.folio.utils.BigDecimalHelper;
import ru.nahk.folio.utils.CalendarHelper;

/**
 * {@link RecyclerView} adapter for position lot items view.
 */
public class LotsListAdapter extends RecyclerView.Adapter<LotsListAdapter.LotItemViewHolder> {
    /**
     * Event listener for lots list item long clicks.
     */
    public interface OnLotItemLongClickListener {
        /**
         * Handles lots list item long click.
         * @param item View-model for the lots list item that is clicked.
         * @param v View that was clicked.
         * @return True if long click was handled, otherwise false.
         */
        boolean onLotItemLongClick(@NonNull LotViewModel item, View v);
    }

    /**
     * Event listener for lot item clicks.
     */
    public interface OnLotItemClickListener {
        /**
         * Callback to run when item is clicked.
         * @param item Lot item that was clicked.
         */
        void onLotClick(LotViewModel item);
    }

    /**
     * View holder for the position lot item.
     */
    static class LotItemViewHolder
        extends RecyclerView.ViewHolder
        implements
            View.OnClickListener,
            View.OnLongClickListener {

        /**
         * Currently bound data.
         */
        private LotViewModel mData;

        /**
         * Control to display change stripe for the lot.
         */
        private final View mStripeView;

        /**
         * Control to display number of shares for the lot.
         */
        private final TextView mQuantityView;

        /**
         * Control to display purchase date for the lot.
         */
        private final TextView mPurchaseDateView;

        /**
         * Control to display current stocks value.
         */
        private final TextView mCurrentValueView;

        /**
         * Control to display value gain or loss.
         */
        private final TextView mValueChangeView;

        /**
         * Listener for item's click events.
         */
        private OnLotItemClickListener mItemClickListener;

        /**
         * Listener for item's long click events.
         */
        private OnLotItemLongClickListener mItemLongClickListener;

        /**
         * Creates new view holder for position lot view.
         * @param itemView Control to display current stock price.
         */
        LotItemViewHolder(View itemView) {
            super(itemView);

            mStripeView = itemView.findViewById(R.id.lot_stripe);
            mQuantityView = itemView.findViewById(R.id.lot_quantity);
            mPurchaseDateView = itemView.findViewById(R.id.lot_purchase_date);
            mCurrentValueView = itemView.findViewById(R.id.lot_current_value);
            mValueChangeView = itemView.findViewById(R.id.lot_value_change);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        /**
         * Handles click events on the associated view by passing click event to external listener.
         * @param v Lot item view.
         */
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onLotClick(mData);
            }
        }

        /**
         * Populates the view control with portfolio position lot data.
         * @param item Details about position lot.
         * @param onClickListener Listener for the lot item click events.
         * @param onLongClickListener Listener for the lot item long click events.
         */
        void bind(
            final LotViewModel item,
            final OnLotItemClickListener onClickListener,
            final OnLotItemLongClickListener onLongClickListener) {

            mData = item;

            mQuantityView.setText(
                itemView.getResources().getQuantityString(
                    R.plurals.lot_shares_count_format,
                    item.quantity,
                    item.quantity,
                    BigDecimalHelper.formatCurrency(item.purchasePrice)));

            mPurchaseDateView.setText(
                CalendarHelper.toString(item.purchaseDate, DateFormat.MEDIUM));

            int valueChangeDirection = 0;

            if (item.currentValue == null) {
                mCurrentValueView.setText(
                    itemView.getResources().getString(R.string.unknown_value));
                mValueChangeView.setText(null);
            } else {
                mCurrentValueView.setText(
                    BigDecimalHelper.formatCurrency(item.currentValue));

                if (item.baseValue == null) {
                    mValueChangeView.setText(null);
                } else {
                    valueChangeDirection =
                        item.currentValue.compareTo(item.baseValue);

                    BigDecimal valueChange = item.currentValue.subtract(item.baseValue);

                    mValueChangeView.setText(
                        String.format(
                            itemView.getContext().getString(R.string.extended_value_change_format),
                            BigDecimalHelper.formatCurrencyChange(valueChange),
                            BigDecimalHelper.formatPercentage(item.baseValue, valueChange)));
                }
            }

            mValueChangeView.setTextAppearance(
                valueChangeDirection > 0
                    ? R.style.TextAppearance_ListItem_Value_Positive
                    : (valueChangeDirection < 0
                        ? R.style.TextAppearance_ListItem_Value_Negative
                        : R.style.TextAppearance_ListItem_Value_NoChange));

            mStripeView.setBackgroundColor(
                ContextCompat.getColor(itemView.getContext(),
                    valueChangeDirection > 0
                        ? R.color.positiveChange
                        : (valueChangeDirection < 0
                            ? R.color.negativeChange
                            : R.color.noChange)));

            mItemClickListener = onClickListener;
            mItemLongClickListener = onLongClickListener;
        }

        /**
         * Handles list item long clicks.
         * @param v List item view.
         * @return True if long click was handled, otherwise false.
         */
        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null) {
                return mItemLongClickListener.onLotItemLongClick(mData, v);
            }

            return false;
        }
    }

    /**
     * All lots for the given position.
     */
    private List<LotViewModel> mLots;

    /**
     * Parent {@link RecyclerView} instance.
     */
    private RecyclerView mRecyclerView;

    /**
     * Listener for lot item click events.
     */
    private OnLotItemClickListener mLotItemClickListener;

    /**
     * Listener for lot item long click events.
     */
    private OnLotItemLongClickListener mLotItemLongClickListener;

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
     * Sets the lots view models data to be displayed.
     * @param lots Lots view models to display.
     */
    public void setData(@NonNull List<LotViewModel> lots) {
        mLots = lots;
    }

    /**
     * Creates new position lot view holder, which hosts the view
     * that displays information about portfolio position lot.
     * @param parent View parent.
     * @param viewType Type of the view to create.
     * @return View holder for the portfolio position lot.
     */
    @Override
    @NonNull
    public LotItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LotItemViewHolder(
            LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.lot_item, parent, false));
    }

    /**
     * Populates position lots view with the information about specific lot.
     * @param holder Position lot view holder.
     * @param position Index of the desired portfolio position lot.
     */
    @Override
    public void onBindViewHolder(@NonNull LotItemViewHolder holder, int position) {
        holder.bind(
            mLots.get(position),
            mLotItemClickListener,
            mLotItemLongClickListener);
    }

    /**
     * Retrieves the total number of portfolio positions.
     * @return Total number of portfolio positions.
     */
    @Override
    public int getItemCount() {
        return mLots == null ? 0 : mLots.size();
    }

    /**
     * Scrolls parent list view of the visible item with given identifier.
     * @param itemId Identifier of the item to scroll to.
     */
    public void scrollToItem(long itemId) {
        if (mRecyclerView != null) {
            for (int itemIndex = 0; itemIndex < mLots.size(); ++itemIndex) {
                if (mLots.get(itemIndex).id == itemId) {
                    mRecyclerView.smoothScrollToPosition(itemIndex);
                    return;
                }
            }
        }
    }

    /**
     * Sets the click listener for position lot items.
     * @param listener On-click listener for position lot items.
     */
    public void setOnLotClickListener(OnLotItemClickListener listener) {
        mLotItemClickListener = listener;
    }

    /**
     * Sets the long click listener for position lot items.
     * @param listener Long click listener for position lot items.
     */
    public void setOnLotLongClickListener(OnLotItemLongClickListener listener) {
        mLotItemLongClickListener = listener;
    }

    /**
     * Deletes an item and returns its index or NULL, if item does not exist.
     * @param item Item to delete.
     * @return Item index or NULL.
     */
    public Integer deleteItem(LotViewModel item) {
        for (int itemIndex = 0; itemIndex < mLots.size(); ++itemIndex){
            if (mLots.get(itemIndex) == item) {
                mLots.remove(itemIndex);
                return itemIndex;
            }
        }

        return null;
    }
}
