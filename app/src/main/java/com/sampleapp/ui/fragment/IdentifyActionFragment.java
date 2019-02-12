package com.sampleapp.ui.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.Device;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;

import java.lang.reflect.Type;

public class IdentifyActionFragment extends BaseFragment implements View.OnClickListener {
    public static final String DEVICE = "device";

    private Device selectedDevice;
    private String serializedDevice;
    private View vFab;

    public static IdentifyActionFragment newInstance(String device) {
        IdentifyActionFragment fragment = new IdentifyActionFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            serializedDevice = getArguments().getString(DEVICE);
            Type type = new TypeToken<Device>() {
            }.getType();
            selectedDevice = new Gson().fromJson(serializedDevice, type);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_identify_action, container, false);

        changeActionBarState(false, true, "");
        initViews(view);
        requestIdentifyAction();

        return view;
    }

    public void requestIdentifyAction() {
        CirrentService
                .getCirrentService()
                .setProgressView(new SimpleProgressDialog(getContext(), "Requesting identify action"))
                .identifyYourself(
                        getContext(),
                        selectedDevice.getDeviceId(),
                        Prefs.SEARCH_TOKEN.getValue(),
                        new CirrentService.DeviceIdentificationCallback() {
                            @Override
                            public void onDeviceIdentificationActionSent() {
                                Toast.makeText(getContext(), R.string.identify_action_requested, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onTokenExpired() {
                                refreshToken(new TokenRefresherCallback() {
                                    @Override
                                    public void onTokenSuccessfullyRefreshed() {
                                        requestIdentifyAction();
                                    }
                                });
                            }
                        }, new CommonErrorCallback() {
                            @Override
                            public void onFailure(CirrentException e) {
                                Toast.makeText(getContext(), getString(R.string.cant_send_ident_action) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void initViews(View view) {
        vFab = view.findViewById(R.id.fab_active);
        vFab.setOnClickListener(this);

        view.findViewById(R.id.btn_yes).setOnClickListener(this);
        view.findViewById(R.id.btn_no).setOnClickListener(this);

        TextView vLightUpAgain = (TextView) view.findViewById(R.id.light_up_again);
        vLightUpAgain.setPaintFlags(vLightUpAgain.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        vLightUpAgain.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                if (selectedDevice.isUserActionEnabled()) {
                    showFragment(PollUserActionFragment.newInstance(serializedDevice), false);
                } else {
                    performBindProcess(selectedDevice);
                }
                break;

            case R.id.btn_no:
                Toast.makeText(getContext(), R.string.couldnt_identify_device, Toast.LENGTH_LONG).show();
                showFragment(new HomeFragment(), false);
                break;

            case R.id.light_up_again:
                requestIdentifyAction();
                break;
        }
    }
}
