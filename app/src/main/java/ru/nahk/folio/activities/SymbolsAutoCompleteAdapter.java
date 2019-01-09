package ru.nahk.folio.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.nahk.folio.R;
import ru.nahk.folio.stockapi.StockApi;
import ru.nahk.folio.stockapi.StockApiException;
import ru.nahk.folio.stockapi.SymbolInfo;

/**
 * List adapter for symbols auto-complete text editor.
 */
final class SymbolsAutoCompleteAdapter extends BaseAdapter implements Filterable {
    /**
     * Current activity context.
     */
    private final Context mContext;

    /**
     * Stock API to search symbols.
     */
    private final StockApi mStockApi;

    /**
     * Symbols search filter.
     */
    private SymbolsFilter mFilter;

    /**
     * List of current auto-complete items.
     */
    private List<SymbolInfo> mSymbols;

    /**
     * Creates a new instance of the {@link SymbolsAutoCompleteAdapter} class
     * with the provided context and stock API.
     * @param context Activity context.
     * @param stockApi Stock API.
     */
    SymbolsAutoCompleteAdapter(@NonNull Context context, @NonNull StockApi stockApi) {
        mContext = context;
        mStockApi = stockApi;
    }

    /**
     * Gets the number of items in the list.
     * @return Number of items in the list.
     */
    @Override
    public int getCount() {
        return mSymbols == null ? 0 : mSymbols.size();
    }

    /**
     * Gets the {@link SymbolInfo} auto-complete item at the given index.
     * @param position Index of the item.
     * @return Item at the given index
     */
    @Override
    public Object getItem(int position) {
        return mSymbols == null ? null : mSymbols.get(position);
    }

    /**
     * Gets the identifier of the item at given index.
     * @param position Index of the item.
     * @return Identifier of the item.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates or populates an existing auto-complete list item view.
     * @param position Index of the item to display in the view.
     * @param convertView Existing view that can be reused.
     * @param parent Parent view.
     * @return List item view that displays an auto-complete item.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // Create new view, if there is nothing to recycle
            convertView =
                LayoutInflater.from(mContext).inflate(R.layout.symbol_autocomplete_item, parent, false);
        }

        SymbolInfo symbolInfo = mSymbols.get(position);

        setTextViewText(convertView, R.id.company_name, symbolInfo.companyName);
        setTextViewText(convertView, R.id.symbol, symbolInfo.symbol);

        View dividerLine = convertView.findViewById(R.id.divider);
        if (dividerLine != null) {
            // Hide divider for the last item
            dividerLine.setVisibility(
                position == getCount() - 1 ? View.GONE : View.VISIBLE);
        }

        return convertView;
    }

    /**
     * Sets the text of the {@link TextView} with the given ID.
     * @param itemView Parent layout view.
     * @param textViewResId Identifier of the {@link TextView}.
     * @param text Text to set.
     */
    private void setTextViewText(View itemView, int textViewResId, String text) {
        TextView textView = itemView.findViewById(textViewResId);

        if (textView != null) {
            textView.setText(text);
        }
    }

    /**
     * Gets the symbols search filter.
     * @return Symbols search filter.
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new SymbolsFilter();
        }

        return mFilter;
    }

    /**
     * Symbols search filter.
     */
    private class SymbolsFilter extends Filter {
        /**
         * Performs {@link StockApi} query to find matching symbols.
         * @param constraint Search constraint.
         * @return Symbols filtering results.
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SymbolInfo> matchingSymbols;

            if (constraint == null || constraint.length() < 1) {
                matchingSymbols = new ArrayList<>();
            }
            else {
                try {
                    matchingSymbols = mStockApi.findSymbols(mContext, constraint.toString());
                } catch (StockApiException apiError) {
                    // Auto-complete is best effort only
                    matchingSymbols = new ArrayList<>();
                }
            }

            FilterResults results = new FilterResults();
            results.count = matchingSymbols.size();
            results.values = matchingSymbols;
            return results;
        }

        /**
         * Publishes filter results to the parent adapter.
         * @param constraint Search constraint.
         * @param results Filtering results.
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mSymbols = (List<SymbolInfo>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
