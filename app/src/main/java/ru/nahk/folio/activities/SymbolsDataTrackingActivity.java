package ru.nahk.folio.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import ru.nahk.folio.broadcasts.SymbolsDataChangedBroadcast;

/**
 * Base class for all activities that are interested in symbols data changes.
 */
abstract class SymbolsDataTrackingActivity extends DataAccessActivityBase {
    /**
     * Receiver for symbols data change notifications.
     */
    private BroadcastReceiver mSymbolsDataChangedReceiver;

    /**
     * Subscribes to local notifications.
     * @param savedInstanceState Previous activity state.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSymbolsDataChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onSymbolsDataChanged();
            }
        };

        // Subscribe to local notifications about symbols data changes
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                mSymbolsDataChangedReceiver,
                new IntentFilter(SymbolsDataChangedBroadcast.ACTION_NAME));
    }

    /**
     * Unsubscribe from all notifications.
     */
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(mSymbolsDataChangedReceiver);

        super.onDestroy();
    }

    /**
     * Handles symbols data change notifications.
     */
    abstract void onSymbolsDataChanged();
}
