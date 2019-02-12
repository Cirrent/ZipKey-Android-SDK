package com.sampleapp.ui.activity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.sampleapp.R;
import com.sampleapp.ui.FragmentListener;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseActivity extends AppCompatActivity implements FragmentListener {

    @Override
    public void showFragment(Fragment fragment, boolean addToBackStack) {
        String fragmentName = fragment.getClass().getName();
        FragmentManager fManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, fragmentName);

        if (addToBackStack && fManager.findFragmentByTag(fragmentName) == null) {
            fragmentTransaction.addToBackStack(fragmentName);
        }

        fragmentTransaction.commit();
    }

    @Override
    public abstract void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title);

    @Override
    public void showToast(int resId, int duration) {
        Toast.makeText(this, R.string.time_limit_exceeded, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void checkForCrashes() {
        CrashManager.register(this);
    }

    public void checkForUpdates() {
        // Remove this for store builds!
//        UpdateManager.register(this);
    }

    public void unregisterManagers() {
        UpdateManager.unregister();
    }

    public void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
