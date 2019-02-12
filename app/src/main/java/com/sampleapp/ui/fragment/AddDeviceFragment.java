package com.sampleapp.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.net.model.Device;
import com.cirrent.cirrentsdk.service.BluetoothService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.EmulatorChecker;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.Utils;
import com.sampleapp.ui.adapter.NearbyDeviceAdapter;

import java.lang.reflect.Type;
import java.util.List;

public class AddDeviceFragment extends BaseFragment implements View.OnClickListener {
    private static final String FOUND_DEVICES = "foundDevices";
    private List<Device> foundDevices;
    private ListView deviceListView;
    private FloatingActionButton layoutFloatingButton;
    private TextView textDeviceAmount;
    private TextView textNoDevicesFound;
    private TextView textSelectProduct;
    private RelativeLayout layoutProductNotListed;
    private AlertDialog onboardingWayDialog;
    private NearbyDeviceAdapter deviceAdapter;

    public static AddDeviceFragment newInstance(String foundDevices) {
        AddDeviceFragment fragment = new AddDeviceFragment();
        Bundle args = new Bundle();
        args.putString(FOUND_DEVICES, foundDevices);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String serializedList = getArguments().getString(FOUND_DEVICES);
            Type collectionType = new TypeToken<List<Device>>() {
            }.getType();
            foundDevices = new Gson().fromJson(serializedList, collectionType);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_device, container, false);

        changeActionBarState(false, true, "");
        initViews(view, inflater);
        showNearDevices();

        return view;
    }

    private void initViews(View view, LayoutInflater inflater) {
        layoutProductNotListed = (RelativeLayout) view.findViewById(R.id.layout_product_not_listed);
        layoutProductNotListed.setOnClickListener(this);
        textNoDevicesFound = (TextView) view.findViewById(R.id.text_no_devices);
        textSelectProduct = (TextView) view.findViewById(R.id.text_select_product);
        deviceListView = (ListView) view.findViewById(R.id.list_nearby_devices);
        deviceListView.addHeaderView(new View(getActivity()));
        deviceListView.addFooterView(new View(getActivity()));
        deviceListView.addFooterView(inflater.inflate(R.layout.footer_add_new_device, null), null, false);
        view.findViewById(R.id.text_product_not_listed).setOnClickListener(this);
        textDeviceAmount = (TextView) view.findViewById(R.id.text_product_amount);
        layoutFloatingButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        layoutFloatingButton.setOnClickListener(this);
    }

    private void showNearDevices() {
        textDeviceAmount.setText(String.valueOf(foundDevices.size()));

        if (!foundDevices.isEmpty()) {
            deviceAdapter = new NearbyDeviceAdapter(getContext(), foundDevices);

            deviceListView.setVisibility(View.VISIBLE);
            deviceListView.setAdapter(deviceAdapter);
            textNoDevicesFound.setVisibility(View.GONE);
            layoutProductNotListed.setVisibility(View.GONE);
            textSelectProduct.setVisibility(View.VISIBLE);

            setupOnNearDeviceClickListener();
        } else {
            textNoDevicesFound.setVisibility(View.VISIBLE);
            layoutProductNotListed.setVisibility(View.VISIBLE);
            textSelectProduct.setVisibility(View.INVISIBLE);
            deviceListView.setVisibility(View.GONE);
        }
    }

    private void setupOnNearDeviceClickListener() {
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deviceAdapter.processClickOnItem(position - 1);
                layoutFloatingButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_action_button:
                Device selectedDevice = deviceAdapter.getSelectedDevice();
                if (selectedDevice.isIdentifyingActionEnabled()) {
                    final String serializedSelectedDevice = new Gson().toJson(selectedDevice);
                    showFragment(IdentifyActionFragment.newInstance(serializedSelectedDevice), false);
                } else if (selectedDevice.isUserActionEnabled()) {
                    final String serializedSelectedDevice = new Gson().toJson(selectedDevice);
                    showFragment(PollUserActionFragment.newInstance(serializedSelectedDevice), false);
                } else {
                    performBindProcess(selectedDevice);
                }
                break;
            case R.id.text_product_not_listed:
                startOnboardingProcess();
                break;
            case R.id.layout_product_not_listed:
                startOnboardingProcess();
                break;
        }
    }

    private void startOnboardingProcess() {
        if (EmulatorChecker.isEmulator()) {
            return;
        }
        String blePrefix = Prefs.WCM_BLE_PREFIX.getValue();
        String softApSsid = Prefs.SOFT_AP_SSID.getValue();
        if (blePrefix.isEmpty() && !softApSsid.isEmpty()) {
            connectViaSoftAp();
        } else if (!blePrefix.isEmpty() && softApSsid.isEmpty()) {
            showFragment(new ConnectViaBluetoothLoadingFragment(), false);
        } else {
            showOnboardingWayDialog();
        }
    }

    private void showOnboardingWayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.choose_way);
        final View dialogBodyView = setupDialogBody();
        dialogBuilder.setView(dialogBodyView);
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onboardingWayDialog.dismiss();
            }
        });
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RadioGroup radioGroup = (RadioGroup) dialogBodyView.findViewById(R.id.radio_group_onboarding_ways);
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.button_via_bluetooth:
                        onboardingWayDialog.dismiss();
                        showFragment(new ConnectViaBluetoothLoadingFragment(), false);
                        break;
                    case R.id.button_via_wifi:
                        onboardingWayDialog.dismiss();
                        connectViaSoftAp();
                        break;
                }
            }
        });

        onboardingWayDialog = dialogBuilder.create();
        onboardingWayDialog.show();
    }

    @NonNull
    private View setupDialogBody() {
        final View dialogBodyView = createDialogBodyView();
        final RadioButton buttonViaBluetooth = (RadioButton) dialogBodyView.findViewById(R.id.button_via_bluetooth);
        if (!BluetoothService.getBluetoothService().isBleSupported(getContext())) {
            buttonViaBluetooth.setEnabled(false);
            buttonViaBluetooth.setText(R.string.bt_not_supported);
        }

        return dialogBodyView;
    }

    private View createDialogBodyView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        return inflater.inflate(R.layout.dialog_onboarding_ways_body, null);
    }

    private void connectViaSoftAp() {
        String ssid = Prefs.SOFT_AP_SSID.getValue();

        getAnalyticsService().logEvent("real_flow", "soft_ap");

        Prefs.PRIVATE_SSID.setValue(Utils.getSsid(getContext()));

        int wifiNetworkId = Utils.getWifiNetworkId(getContext());
        Prefs.WIFI_NETWORK_ID.setValue(wifiNetworkId);

        if (ssid.isEmpty()) {
            Toast.makeText(getContext(), R.string.conf_both_empty, Toast.LENGTH_SHORT).show();
        } else {
            showFragment(ConnectViaSoftApLoadingFragment.newInstance(ssid), false);
        }
    }
}
