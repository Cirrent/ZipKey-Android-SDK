package com.sampleapp.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cirrent.cirrentsdk.service.BluetoothService;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.cirrent.cirrentsdk.service.LocationService;
import com.cirrent.cirrentsdk.service.SoftApService;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.ui.activity.demo.DemoActivity;
import com.sampleapp.ui.fragment.ConfigurationFragment;
import com.sampleapp.ui.fragment.ConnectViaBluetoothLoadingFragment;
import com.sampleapp.ui.fragment.ConnectViaSoftApLoadingFragment;
import com.sampleapp.ui.fragment.ConnectedToSoftApFragment;
import com.sampleapp.ui.fragment.DisconnectFromSoftApLoadingFragment;
import com.sampleapp.ui.fragment.HomeFragment;
import com.sampleapp.ui.fragment.PollUserActionFragment;
import com.sampleapp.ui.fragment.SendCredentialsViaBluetoothFragment;
import com.sampleapp.ui.fragment.SendCredentialsViaSoftApFragment;
import com.sampleapp.ui.fragment.SendPrivateCredentialsFragment;
import com.sampleapp.ui.fragment.SendProviderCredentialsFragment;
import com.sampleapp.ui.fragment.SetupDeviceViaSoftApFragment;

import java.util.Arrays;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static boolean isLocationServiceAllowed = true;
    private boolean backArrowListenerIsRegistered = false;
    private ActionBarDrawerToggle toggle;
    private ActionBar supportActionBar;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = setupActionBar();
        setupNavigationDrawer(toolbar);
        checkForUpdates();
    }

    private Toolbar setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.home_title));
        }
        return toolbar;
    }

    private void setupNavigationDrawer(Toolbar toolbar) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkForCrashes();

        final String softApSsid = Prefs.SOFT_AP_SSID.getValue();
        final String currentSsid = Utils.getSsid(this);
        if (currentSsid.equals(softApSsid)) {
            //disconnect from the soft ap network if phone/tablet is connected to
            showFragment(DisconnectFromSoftApLoadingFragment.newInstance(softApSsid), false);
        } else if (Prefs.SOFT_AP_DEVICE_SETUP_DATA.exists()) {
            continueInterruptedDeviceSetup();
        } else {
            final FragmentManager supportFragmentManager = getSupportFragmentManager();
            Fragment homeFragment = supportFragmentManager.findFragmentByTag(HomeFragment.class.getName());
            if (homeFragment == null || !homeFragment.isVisible()) {
                showFragment(new HomeFragment(), false);
            }
        }
    }

    private void continueInterruptedDeviceSetup() {
        String serializedSetupData = Prefs.SOFT_AP_DEVICE_SETUP_DATA.getValue();
        showFragment(SendCredentialsViaSoftApFragment.newInstance(serializedSetupData), false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterManagers();

        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .cancelAllTasks();
        //---------------------------

        //----- SDK call ------------
        SoftApService
                .getSoftApService()
                .cancelAllTasks();
        //---------------------------

        //----- SDK call ------------
        BluetoothService
                .getBluetoothService()
                .cancelAllTasks();
        //---------------------------

        //----- SDK call ------------
        LocationService.getLocationService().stopLocationService();
        //---------------------------
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterManagers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title) {
        if (hideActionBar) {
            supportActionBar.hide();
            return;
        } else {
            supportActionBar.show();
        }

        setActionBarTitle(title);

        if (enableBackArrow) {
            showBackArrowIcon();

            if (!backArrowListenerIsRegistered) {
                setBackArrowOnClickListener();
                backArrowListenerIsRegistered = true;
            }
        } else {
            showHamburgerIcon();
        }
    }

    private void setActionBarTitle(String title) {
        if (title.isEmpty()) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        } else {
            supportActionBar.setDisplayShowTitleEnabled(true);
            supportActionBar.setTitle(title);
        }
    }

    private void showBackArrowIcon() {
        toggle.setDrawerIndicatorEnabled(false);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setBackArrowOnClickListener() {
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSoftApFragmentShowed(getSupportFragmentManager())) {
                    showFragment(new HomeFragment(), false);
                }
            }
        });
    }

    private void showHamburgerIcon() {
        supportActionBar.setDisplayHomeAsUpEnabled(false);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.setToolbarNavigationClickListener(null);
        backArrowListenerIsRegistered = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LocationService.LOCATION_REQUEST_CODE && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            isLocationServiceAllowed = false;
        }
        //----- SDK call ------------
        LocationService.getLocationService().onRequestPermissionsResult(requestCode, grantResults, this);
        //---------------------------
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            final FragmentManager supportFragmentManager = getSupportFragmentManager();
            Fragment homeFragment = supportFragmentManager.findFragmentByTag(HomeFragment.class.getName());
            if (homeFragment != null) {
                if (homeFragment.isVisible()) {
                    finish();
                } else {
                    handleOnBackPressed(supportFragmentManager);
                }
            } else {
                handleOnBackPressed(supportFragmentManager);
            }
        }
    }

    private void handleOnBackPressed(FragmentManager supportFragmentManager) {
        if (isSoftApFragmentShowed(supportFragmentManager)) return;

        List<Fragment> fragmentsWithBlockedBackNavigation = Arrays.asList(
                supportFragmentManager.findFragmentByTag(PollUserActionFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(SendPrivateCredentialsFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(SendProviderCredentialsFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(SendCredentialsViaSoftApFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(ConnectViaSoftApLoadingFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(DisconnectFromSoftApLoadingFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(ConnectViaBluetoothLoadingFragment.class.getName()),
                supportFragmentManager.findFragmentByTag(SendCredentialsViaBluetoothFragment.class.getName())
        );

        for (Fragment fragment : fragmentsWithBlockedBackNavigation) {
            if (fragment != null && fragment.isVisible()) {
                Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showFragment(new HomeFragment(), false);
    }

    private boolean isSoftApFragmentShowed(FragmentManager supportFragmentManager) {
        final Fragment connectedToSoftApFragment = supportFragmentManager.findFragmentByTag(ConnectedToSoftApFragment.class.getName());
        if (connectedToSoftApFragment != null && connectedToSoftApFragment.isVisible()) {
            showFragment(DisconnectFromSoftApLoadingFragment.newInstance(Prefs.SOFT_AP_SSID.getValue()), false);
            return true;
        }

        final Fragment setupViaSoftApFragment = supportFragmentManager.findFragmentByTag(SetupDeviceViaSoftApFragment.class.getName());
        if (setupViaSoftApFragment != null && setupViaSoftApFragment.isVisible()) {
            showFragment(DisconnectFromSoftApLoadingFragment.newInstance(Prefs.SOFT_AP_SSID.getValue()), false);
            return true;
        }
        return false;
    }

    @Override
    public void showFragment(Fragment fragment, boolean addToBackStack) {
        super.showFragment(fragment, addToBackStack);

        if (drawer != null) {
            if (fragment instanceof HomeFragment || fragment instanceof ConfigurationFragment) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            } else {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_products:
                closeDrawer();
                showFragment(new HomeFragment(), false);

                return true;
            case R.id.nav_walkthru:
                startActivity(new Intent(this, DemoActivity.class));
                closeDrawer();

                return true;
            case R.id.nav_configuration:
                closeDrawer();
                showFragment(new ConfigurationFragment(), false);
                return true;
            case R.id.nav_log_out:
                closeDrawer();
                sanitizePreferences();
                Intent intent = new Intent(this, StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return true;
        }

        return true;
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void sanitizePreferences() {
        Prefs.APP_ID.remove();
        Prefs.ENCODED_CREDENTIALS.remove();
        Prefs.MANAGE_TOKEN.remove();
        Prefs.SEARCH_TOKEN.remove();
        Prefs.BIND_TOKEN.remove();
        Prefs.SOFT_AP_SSID.remove();
        Prefs.FRIENDLY_NAMES.remove();
        Prefs.WIFI_NETWORK_ID.remove();
        Prefs.LOCATION_WARNING_SHOWN.remove();
        Prefs.PRIVATE_SSID.remove();
        Prefs.ACCOUNT_ID.remove();
    }
}
