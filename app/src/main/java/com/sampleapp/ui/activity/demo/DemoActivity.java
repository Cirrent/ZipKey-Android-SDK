package com.sampleapp.ui.activity.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.sampleapp.R;
import com.sampleapp.ui.activity.BaseActivity;
import com.sampleapp.ui.fragment.demo.DemoLookingForAcmeProductsFragment;
import com.sampleapp.ui.fragment.demo.DemoSendCredentialsFragment;
import com.sampleapp.ui.fragment.demo.DemoStartFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DemoActivity extends BaseActivity {

    private ActionBar supportActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        setupActionBar();

        showFragment(new DemoStartFragment(), false);
        checkForUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkForCrashes();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterManagers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterManagers();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_icon_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_close:
                finish();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title) {
        if (hideActionBar) {
            supportActionBar.hide();
            return;
        } else {
            supportActionBar.show();
        }

        if (title.isEmpty()) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        } else {
            supportActionBar.setDisplayShowTitleEnabled(true);
            supportActionBar.setTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        skipFragmentIfItInBackStack(DemoSendCredentialsFragment.class.getName());
        skipFragmentIfItInBackStack(DemoLookingForAcmeProductsFragment.class.getName());
    }

    private void skipFragmentIfItInBackStack(String fragmentName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int index = fragmentManager.getBackStackEntryCount() - 1;
        if (index >= 0 && fragmentManager.getBackStackEntryAt(index).getName().equals(fragmentName)) {
            super.onBackPressed();
        }
    }

}
