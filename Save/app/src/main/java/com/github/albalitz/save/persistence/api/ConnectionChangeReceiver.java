package com.github.albalitz.save.persistence.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.albalitz.save.utils.Utils;

/**
 * Created by albalitz on 8/16/17.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(this.toString(), "Connection changed!");
        if (Utils.networkAvailable(context)) {
            OfflineQueue.saveQueuedLinks();
        } else {
            Log.i(this.toString(), "Not connected.");
        }
    }
}
