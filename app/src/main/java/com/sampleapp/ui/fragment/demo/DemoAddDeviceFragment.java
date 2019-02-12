package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.demo.DemoDevice;
import com.sampleapp.ui.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class DemoAddDeviceFragment extends BaseFragment implements View.OnClickListener {

    private int connectionMethod;

    private ListView deviceListView;
    private TextView textDeviceAmount;
    private TextView textNoDevicesFound;
    private TextView textSelectProduct;
    private RelativeLayout layoutProductNotListed;
    private FloatingActionButton floatingActionButton;

    private DemoNearbyDeviceAdapter deviceAdapter;

    public static DemoAddDeviceFragment newInstance(int connectionMethod) {
        DemoAddDeviceFragment fragment = new DemoAddDeviceFragment();
        Bundle args = new Bundle();
        args.putInt(DemoStartFragment.CONNECTION_METHOD_CODE, connectionMethod);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            connectionMethod = getArguments().getInt(DemoStartFragment.CONNECTION_METHOD_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_demo_add_device, container, false);

        changeActionBarState(false, true, getString(R.string.walkthru_mode));
        initViews(view, inflater);
        showNearDevices();

        return view;
    }

    private List<DemoDevice> getDevicesStub() {
        List<DemoDevice> foundDevices = new ArrayList<>();

        DemoDevice device1 = new DemoDevice();
        device1.setProviderLogo(R.drawable.smanos_logo_3);
        device1.setDeviceId("Product 1");
        device1.setDeviceName("Camera");
        device1.setImageURL(String.valueOf(R.drawable.camera_1));
        foundDevices.add(device1);

        if (connectionMethod == DemoStartFragment.CONNECTION_METHOD_1
                || connectionMethod == DemoStartFragment.CONNECTION_METHOD_2) {
            DemoDevice device2 = new DemoDevice();
            device2.setProviderLogo(R.drawable.smanos_logo_3);
            device2.setDeviceId("Product 2");
            device2.setDeviceName("Smart Hub");
            device2.setImageURL(String.valueOf(R.drawable.speaker_3));
            foundDevices.add(device2);
        }

        return foundDevices;
    }

    private void initViews(View view, LayoutInflater inflater) {
        layoutProductNotListed = (RelativeLayout) view.findViewById(R.id.layout_product_not_listed);
        layoutProductNotListed.setOnClickListener(this);
        textNoDevicesFound = (TextView) view.findViewById(R.id.text_no_devices);
        textSelectProduct = (TextView) view.findViewById(R.id.text_select_product);
        deviceListView = (ListView) view.findViewById(R.id.list_nearby_devices);
        deviceListView.addHeaderView(new View(getActivity()));
        deviceListView.addFooterView(new View(getActivity()));
        View footerView = inflater.inflate(R.layout.footer_add_new_device, null);
        deviceListView.addFooterView(footerView, null, false);
        view.findViewById(R.id.text_product_not_listed).setOnClickListener(this);
        textDeviceAmount = (TextView) view.findViewById(R.id.text_product_amount);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(this);
    }

    private void showNearDevices() {
        List<DemoDevice> foundDevices = getDevicesStub();

        textDeviceAmount.setText(String.valueOf(foundDevices.size()));

        if (!foundDevices.isEmpty()) {
            deviceAdapter = new DemoNearbyDeviceAdapter(getContext(), foundDevices);

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
        deviceAdapter.setOnItemClickListener(new DemoNearbyDeviceAdapter.OnItemClick() {
            @Override
            public void itemSelectionChanged() {
                if (deviceAdapter.isAnyDeviceSelected()) {
                    floatingActionButton.setVisibility(View.GONE);
                } else {
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void processDeviceInfo(DemoDevice device) {
                Integer deviceImageResId = Integer.valueOf(device.getImageURL());
                showFragment(DemoDeviceInfoFragment.newInstance(
                        device.getDeviceId(), deviceImageResId), true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        ArrayList<DemoDevice> selectedDevices = deviceAdapter.getSelectedDevices();

        switch (v.getId()) {
            case R.id.floating_action_button:
                switch (connectionMethod) {
                    case DemoStartFragment.CONNECTION_METHOD_1:
                        showFragment(DemoSetupDeviceAutomaticallyFragment.newInstance(
                                connectionMethod,
                                selectedDevices,
                                "XYZ",
                                "XFINITY"
                        ), true);
                        break;
                    case DemoStartFragment.CONNECTION_METHOD_2:
                    case DemoStartFragment.CONNECTION_METHOD_3:
                        showFragment(DemoSetupDeviceManuallyFragment.newInstance(
                                connectionMethod, selectedDevices), true);
                        break;
                }
                break;

            case R.id.text_product_not_listed:
            case R.id.layout_product_not_listed:
                connectToDeviceViaSoftAp();
                break;
        }
    }

    private void connectToDeviceViaSoftAp() {
        showFragment(DemoLookingForAcmeProductsFragment
                .newInstance(DemoStartFragment.CONNECTION_METHOD_3), true);
    }

}
