package com.sampleapp.net;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.cirrent.cirrentsdk.internal.net.util.NetUtils;

public class InternetConnectionChecker {

    private static final String TAG = InternetConnectionChecker.class.getSimpleName();
    private static final int MAX_ATTEMPTS_TO_CONNECT = 30;
    private static final int DELAY_MILLIS = 1000;

    private boolean callAfterReceiverWasRegistered = false;
    private int attemptsCount = 0;
    private Listener listener;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            listener.tryToConnect();
        }
    };
    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (callAfterReceiverWasRegistered) {
                callAfterReceiverWasRegistered = false;
            } else {
                failedToReachCloud(context);
            }
        }
    };

    public InternetConnectionChecker(Listener listener) {
        this.listener = listener;
    }

    public void registerReceiver(Activity activity) {
        callAfterReceiverWasRegistered = true;
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        activity.registerReceiver(networkChangeReceiver, intentFilter);
    }

    public void unregisterReceiver(Activity activity) {
        activity.unregisterReceiver(networkChangeReceiver);
    }

    public void failedToReachCloud(Context context) {
        boolean connectedToNetwork = new NetUtils(context).isConnectedToNetwork();

        if (connectedToNetwork && attemptsCount < MAX_ATTEMPTS_TO_CONNECT) {
            attemptsCount++;
            String msg = "Network was connected, will try to connect in " + DELAY_MILLIS / 1000
                    + " seconds," + " attempt " + attemptsCount + " of " + MAX_ATTEMPTS_TO_CONNECT;
            Log.d(TAG, msg);
            handler.postDelayed(runnable, DELAY_MILLIS);
        } else {
            attemptsCount = 0;
            Log.d(TAG, "Network was failedToReachCloud, no more attempting to connect");
            listener.unconnected();
        }
    }

    public interface Listener {
        void tryToConnect();

        void unconnected();
    }
}
