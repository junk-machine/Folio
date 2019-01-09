package ru.nahk.folio.broadcasts;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Local broadcast to notify about symbols data changes.
 */
public final class SymbolsDataChangedBroadcast {
    /**
     * Name of the intent action.
     */
    public static final String ACTION_NAME = "symbols-data-changed";

    /**
     * Sends the broadcast.
     * @param context Activity or application context.
     */
    public static void send(Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_NAME));
    }
}
