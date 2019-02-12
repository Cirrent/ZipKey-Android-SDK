package com.sampleapp.ui;

import android.support.v4.app.Fragment;

public interface FragmentListener {
    void showFragment(Fragment fragment, boolean addToBackstack);

    void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title);

    void showToast(int resId, int duration);
}
