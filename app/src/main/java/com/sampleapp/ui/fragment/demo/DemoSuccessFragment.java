package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.demo.DemoDevice;
import com.sampleapp.ui.fragment.BaseFragment;

import java.util.ArrayList;

public class DemoSuccessFragment extends BaseFragment implements View.OnClickListener {

    private static final String DEVICES = "devices";
    private static final String NETWORK_SSID = "networkSsid";

    private int connectionMethod;
    private ArrayList<DemoDevice> devices;
    private String networkSsid;

    private TextView textSuccessInfo;
    private EditText editDeviceName;
    private ImageView imageProviderLogo;
    private TextView textProviderInfo;
    private EditText editDeviceNameSecond;
    private TextView textDeviceNameHint;
    private TextView textDeviceNameHintSecond;
    private View lineSecond;

    public static DemoSuccessFragment newInstance(int connectionMethod, ArrayList<DemoDevice> devices, String networkSsid) {
        DemoSuccessFragment fragment = new DemoSuccessFragment();
        Bundle args = new Bundle();
        args.putInt(DemoStartFragment.CONNECTION_METHOD_CODE, connectionMethod);
        args.putSerializable(DEVICES, devices);
        args.putString(NETWORK_SSID, networkSsid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            connectionMethod = arguments.getInt(DemoStartFragment.CONNECTION_METHOD_CODE);
            devices = (ArrayList<DemoDevice>) arguments.getSerializable(DEVICES);
            networkSsid = arguments.getString(NETWORK_SSID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_success, container, false);

        changeActionBarState(false, true, getString(R.string.walkthru_mode));
        initViews(view);
        showProviderInfoIfNeed(view);
        fillSuccessInfo();

        return view;
    }

    private void initViews(View view) {
        view.findViewById(R.id.provider_info).setOnClickListener(this);

        textSuccessInfo = (TextView) view.findViewById(R.id.text_success_info);
        editDeviceName = (EditText) view.findViewById(R.id.edit_device_name);
        textDeviceNameHint = (TextView) view.findViewById(R.id.text_hint_device_name);

        editDeviceNameSecond = (EditText) view.findViewById(R.id.edit_device_name_second);
        textDeviceNameHintSecond = (TextView) view.findViewById(R.id.text_hint_device_name_second);
        lineSecond = (View) view.findViewById(R.id.line_edit_device_name_second);

        imageProviderLogo = (ImageView) view.findViewById(R.id.img_success_provider_logo);
        textProviderInfo = (TextView) view.findViewById(R.id.text_success_provider_info);

        Button buttonAddAnotherProduct = (Button) view.findViewById(R.id.button_add_another_product);
        buttonAddAnotherProduct.setVisibility(View.GONE);
        buttonAddAnotherProduct.setOnClickListener(this);

        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(this);
    }

    private void showProviderInfoIfNeed(View view) {
        switch (connectionMethod) {
            case DemoStartFragment.CONNECTION_METHOD_1:
            case DemoStartFragment.CONNECTION_METHOD_2:
                imageProviderLogo.setImageResource(R.drawable.comcast_xfinity_logo);
                textProviderInfo.setText(R.string.acme_used_xfinity_with_zipkey_to_simplify_your_setup_learn_more);
                view.findViewById(R.id.provider_info).setVisibility(View.VISIBLE);
                break;
            default:
                view.findViewById(R.id.provider_info).setVisibility(View.GONE);
        }
    }

    private void fillSuccessInfo() {
        final String nameYour = "Name your ";

        DemoDevice device1 = devices.get(0);
        String msg = device1.getDeviceId() + " " + getString(R.string.is_now_connected_to) + " " + networkSsid;
        editDeviceName.setText(device1.getDeviceId());
        textDeviceNameHint.setText(nameYour + device1.getDeviceId());

        if (devices.size() >= 2) {
            DemoDevice device2 = devices.get(1);
            msg = "Products" + " " + getString(R.string.is_now_connected_to) + " " + networkSsid;

            editDeviceNameSecond.setVisibility(View.VISIBLE);
            textDeviceNameHintSecond.setVisibility(View.VISIBLE);
            lineSecond.setVisibility(View.VISIBLE);

            editDeviceNameSecond.setText(device2.getDeviceId());
            textDeviceNameHintSecond.setText(nameYour + device2.getDeviceId());
        }

        textSuccessInfo.setText(msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.provider_info:
                showFragment(new DemoAcmeZipkeyTalkFragment(), true);
                break;

            case R.id.button_add_another_product:
                getActivity().getSupportFragmentManager().popBackStack(
                        DemoAddDeviceFragment.class.getName(),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                showFragment(DemoAddDeviceFragment.newInstance(connectionMethod), false);
                break;

            case R.id.floating_action_button:
                showFragment(DemoStatisticFragment.newInstance(connectionMethod), true);
                break;
        }
    }

}
