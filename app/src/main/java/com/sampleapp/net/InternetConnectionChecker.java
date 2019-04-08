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

    public static final String TAG = InternetConnectionChecker.class.getSimpleName();
    private final int maxAttemptsCount = 2 * 60;
    private Listener listener;
    private int attemptsCount = 0;
    private int delayForNextCheckingInternetConnection = 1000;
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
            networkStateChanged(context);
        }
    };

    public InternetConnectionChecker(Listener listener) {
        this.listener = listener;
    }

    public void registerReceiver(Activity activity) {
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        activity.registerReceiver(networkChangeReceiver, intentFilter);
    }

    public void unregisterReceiver(Activity activity) {
        activity.unregisterReceiver(networkChangeReceiver);
    }

    public void failedToReachCloud() {
        if (attemptsCount < maxAttemptsCount) {
            attemptsCount++;
            Log.d(TAG, "Network was connected, will request managed devices after " + delayForNextCheckingInternetConnection / 1000 + " seconds, attempt " + attemptsCount);
            handler.postDelayed(runnable, delayForNextCheckingInternetConnection);
        } else {
            listener.unconnected();
        }
    }

    private void networkStateChanged(Context context) {
        boolean connectedToNetwork = new NetUtils(context).isConnectedToNetwork();

        attemptsCount = 0;
        if (connectedToNetwork) {
            attemptsCount++;
            Log.d(TAG, "Network was connected, will request managed devices after " + delayForNextCheckingInternetConnection / 1000 + " seconds, attempt " + attemptsCount);
            handler.postDelayed(runnable, delayForNextCheckingInternetConnection);
        } else {
            Log.d(TAG, "Network was failedToReachCloud, will refresh screen");
            listener.unconnected();
        }
    }

    public interface Listener {
        void tryToConnect();

        void unconnected();
    }

}
