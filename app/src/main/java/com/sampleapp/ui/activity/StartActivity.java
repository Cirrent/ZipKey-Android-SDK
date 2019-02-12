package com.sampleapp.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

import com.sampleapp.R;
import com.sampleapp.ui.fragment.StartFragment;

public class StartActivity extends BaseActivity {

    private ActionBar supportActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        setupActionBar();

        showFragment(new StartFragment(), false);
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
            supportActionBar.hide();
        }
    }

    @Override
    public void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title) {
        if (hideActionBar) {
            supportActionBar.hide();
            hideStatusBar();
            return;
        } else {
            supportActionBar.show();
            showStatusBar();
        }

        if (title.isEmpty()) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        } else {
            supportActionBar.setDisplayShowTitleEnabled(true);
            supportActionBar.setTitle(title);
        }

        supportActionBar.setDisplayHomeAsUpEnabled(enableBackArrow);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
    }

    private void showStatusBar() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
