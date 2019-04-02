package com.sampleapp.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cirrent.cirrentsdk.service.CirrentService;
import com.cirrent.cirrentsdk.service.LocationService;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.net.model.ManagedDeviceList;
import com.sampleapp.net.requester.ManagedDevicesRequester;
import com.sampleapp.ui.activity.DeviceInfoActivity;
import com.sampleapp.ui.activity.HomeActivity;
import com.sampleapp.ui.adapter.ManagedDeviceAdapter;

import java.util.List;

import co.stkotok.swipetodelete.STDItemCallback;
import co.stkotok.swipetodelete.STDItemDecoration;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = HomeFragment.class.getSimpleName();
    private RecyclerView devicesRecyclerView;
    private TextView textNoManagedDevices;
    private TextView textUnableToReachCloud;
    private Button buttonRetry;
    private FloatingActionButton buttonFloating;
    private AlertDialog softApSsidDialog;
    private ManagedDeviceAdapter devicesAdapter;

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean internetConnected = false;

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                Log.d(TAG, "ConnectivityManager is null");
                return;
            }
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                boolean wifiConnected = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                boolean mobileConnected = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
                if (wifiConnected || mobileConnected) {
                    internetConnected = true;
                }
            }

            Log.d(TAG, "Internet was " + (internetConnected ? "" : "dis") + "connected");
            refreshViews(internetConnected);
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        changeActionBarState(false, false, getString(R.string.home_title));
        initViews(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (HomeActivity.isLocationServiceAllowed) {
            //----- SDK call ------------
            CirrentService.getCirrentService().gatherEnvironment(getActivity(), Prefs.APP_ID.getValue());
            //---------------------------
        }

        getManagedDevices(false);
        checkLocationService();

        Menu sideMenu = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu();
        sideMenu.findItem(R.id.nav_products).setChecked(true);

        registerNetworkChangeReceiver();
    }

    private void registerNetworkChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(networkChangeReceiver);
    }

    public void checkLocationService() {
        if (!Prefs.LOCATION_WARNING_SHOWN.getValue()) {
            if (!LocationService.getLocationService().isLocationEnabled()) {
                softApSsidDialog = new AlertDialog.Builder(getContext())
                        .setCustomTitle(createDialogHeaderView())
                        .setMessage(R.string.dialog_location_service_body)
                        .setCancelable(false)
                        .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(settingsIntent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                softApSsidDialog.dismiss();
                            }
                        })
                        .create();

                softApSsidDialog.show();

                Prefs.LOCATION_WARNING_SHOWN.setValue(true);
            }
        }
    }

    private View createDialogHeaderView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        return inflater.inflate(R.layout.dialog_location_header, null);
    }

    private void initViews(View view) {
        initDevicesList(view);

        textNoManagedDevices = (TextView) view.findViewById(R.id.text_no_managed_devices);
        textUnableToReachCloud = (TextView) view.findViewById(R.id.text_unable_to_reach_cloud);
        buttonRetry = (Button) view.findViewById(R.id.button_retry);
        buttonFloating = (FloatingActionButton) view.findViewById(R.id.floating_action_button);

        buttonFloating.setOnClickListener(this);
        buttonRetry.setOnClickListener(this);
    }

    private void initDevicesList(View view) {
        devicesRecyclerView = (RecyclerView) view.findViewById(R.id.list_managed_devices);
        int itemRemovalBackgroundColor = ContextCompat.getColor(getContext(), R.color.red);

        devicesAdapter = new ManagedDeviceAdapter(getContext(), itemRemovalBackgroundColor);
        devicesRecyclerView.setAdapter(devicesAdapter);

        devicesAdapter.setOnItemClickListener(new ManagedDeviceAdapter.OnClick() {
            @Override
            public void onListItem(ManagedDeviceList.ProductCloudDevice device) {
                final Intent intent = new Intent(getContext(), DeviceInfoActivity.class);
                intent.putExtra(DeviceInfoActivity.DEVICE_ID, device.getDeviceId());
                intent.putExtra(DeviceInfoActivity.DEVICE_IMAGE_URL, device.getImageUrl());
                startActivityForResult(intent, DeviceInfoActivity.REMOVE_REQUEST_CODE);
            }
        });

        devicesAdapter.setOnDeviceResettedListener(new ManagedDeviceAdapter.DeviceResettedListener() {
            @Override
            public void onResetted(String deviceId, boolean isSuccessful) {
                getManagedDevices(false);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new STDItemCallback<>(
                R.drawable.ic_delete, (int) getResources().getDimension(R.dimen.activity_horizontal_margin), itemRemovalBackgroundColor, devicesAdapter
        ));
        itemTouchHelper.attachToRecyclerView(devicesRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        devicesRecyclerView.setLayoutManager(layoutManager);
        devicesRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        devicesRecyclerView.addItemDecoration(new STDItemDecoration(itemRemovalBackgroundColor));
    }

    public void getManagedDevices(final boolean isRetryButtonClicked) {
        new ManagedDevicesRequester(getContext(), Prefs.ENCODED_CREDENTIALS.getValue()) {
            @Override
            public void onSuccess(final String manageToken, final List<ManagedDeviceList.ProductCloudDevice> managedDevices) {
                textUnableToReachCloud.setVisibility(View.GONE);
                buttonRetry.setVisibility(View.GONE);
                buttonFloating.setVisibility(View.VISIBLE);

                if (managedDevices.isEmpty()) {
                    devicesRecyclerView.setVisibility(View.GONE);
                    textNoManagedDevices.setVisibility(View.VISIBLE);
                } else {
                    textNoManagedDevices.setVisibility(View.GONE);
                    textUnableToReachCloud.setVisibility(View.GONE);
                    buttonRetry.setVisibility(View.GONE);

                    showManagedDevices(managedDevices);
                }

                if (Prefs.SOFT_AP_DEVICE_SETUP_DATA.exists() && isRetryButtonClicked) {
                    continueInterruptedDeviceSetup();
                }
            }

            @Override
            public void failedToReachCloud(String error) {
                super.failedToReachCloud(error);

                refreshViews(false);
            }
        }.doRequest(new SimpleProgressDialog(getContext(), getString(R.string.getting_managed_devices)));
    }

    private void refreshViews(boolean internetConnected) {
        if (internetConnected) {
            textUnableToReachCloud.setVisibility(View.GONE);
            buttonRetry.setVisibility(View.GONE);
            getManagedDevices(false);
        } else {
            buttonFloating.setVisibility(View.GONE);
            devicesRecyclerView.setVisibility(View.GONE);
            textNoManagedDevices.setVisibility(View.GONE);
            textUnableToReachCloud.setVisibility(View.VISIBLE);
            buttonRetry.setVisibility(View.VISIBLE);
        }
    }


    private void continueInterruptedDeviceSetup() {
        String serializedSetupData = Prefs.SOFT_AP_DEVICE_SETUP_DATA.getValue();
        showFragment(SendCredentialsViaSoftApFragment.newInstance(serializedSetupData), false);
    }

    private void showManagedDevices(List<ManagedDeviceList.ProductCloudDevice> managedDevices) {
        devicesRecyclerView.setVisibility(View.VISIBLE);
        devicesAdapter.setDevices(managedDevices);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_action_button:
                showFragment(new FindDeviceFragment(), false);
                break;
            case R.id.button_retry:
                getManagedDevices(true);
                break;
        }
    }
}
