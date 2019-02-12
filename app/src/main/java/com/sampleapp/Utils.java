package com.sampleapp;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Utils {
    public static String encodeCredentialsToBase64(String login, String password) {
        final String prefix = "Basic ";
        final String concatenatedCredentials = login + ":" + password;
        final String encodedCredentials = prefix + Base64.encodeToString(concatenatedCredentials.getBytes(), Base64.NO_WRAP);
        return encodedCredentials;
    }

    public static boolean isWifiEnabled(Context context) {
        final boolean isWifiEnabled = getWifiManager(context).isWifiEnabled();
        return isWifiEnabled;
    }

    public static String getSsid(Context context) {
        WifiInfo wifiInfo = getWifiManager(context).getConnectionInfo();
        String ssid = "";
        if (wifiInfo != null) {
            String dirtySsid = wifiInfo.getSSID();
            ssid = dirtySsid.replace("\"", "");
        }
        return ssid.trim();
    }

    public static int getWifiNetworkId(Context context) {
        WifiInfo wifiInfo = getWifiManager(context).getConnectionInfo();

        return wifiInfo.getNetworkId();
    }

    public static String generateUniqueId() {
        return String.valueOf(UUID.randomUUID());
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static ObjectAnimator startConstantVerticalViewAnimation(View view) {
        ObjectAnimator flipAnimator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);
        flipAnimator.setDuration(1000);
        flipAnimator.setRepeatMode(ObjectAnimator.RESTART);
        flipAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flipAnimator.start();
        return flipAnimator;
    }

    public static void setFriendlyName(String deviceId, String friendlyName) {
        Gson gson = new Gson();

        Map<String, String> names;
        if (Prefs.FRIENDLY_NAMES.exists()) {
            String serializedNames = Prefs.FRIENDLY_NAMES.getValue();
            Type collectionType = new TypeToken<Map<String, String>>() {
            }.getType();
            names = gson.fromJson(serializedNames, collectionType);
        } else {
            names = new HashMap<>(1);
        }

        names.put(deviceId, friendlyName.trim());
        Prefs.FRIENDLY_NAMES.setValue(gson.toJson(names));
    }

    public static String getFriendlyName(String deviceId) {
        String friendlyName;
        if (Prefs.FRIENDLY_NAMES.exists()) {
            String serializedNames = Prefs.FRIENDLY_NAMES.getValue();
            Type collectionType = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> names = new Gson().fromJson(serializedNames, collectionType);
            friendlyName = names.get(deviceId);
        } else {
            friendlyName = deviceId;
        }

        return friendlyName == null ? deviceId : friendlyName;
    }

    public static void makeLinks(TextView textView, Map<String, ClickableSpan> links) {
        if (textView == null) return;

        String wholeText = String.valueOf(textView.getText());
        SpannableString ss = new SpannableString(wholeText);

        for (Map.Entry<String, ClickableSpan> entry : links.entrySet()) {
            int linkIndexStart = wholeText.indexOf(entry.getKey());
            int linkIndexEnd = linkIndexStart + entry.getKey().length();
            ss.setSpan(entry.getValue(), linkIndexStart, linkIndexEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(ss, TextView.BufferType.SPANNABLE);
    }

}
