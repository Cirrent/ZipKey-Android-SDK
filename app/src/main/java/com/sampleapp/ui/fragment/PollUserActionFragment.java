package com.sampleapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.Device;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampleapp.Prefs;
import com.sampleapp.R;

import java.lang.reflect.Type;

public class PollUserActionFragment extends BaseFragment {

    public static final String DEVICE = "device";

    private Device selectedDevice;
    private View vFab;

    public static PollUserActionFragment newInstance(String device) {
        PollUserActionFragment fragment = new PollUserActionFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            String serializedDevice = getArguments().getString(DEVICE);
            Type type = new TypeToken<Device>() {
            }.getType();
            selectedDevice = new Gson().fromJson(serializedDevice, type);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_press_device_button, container, false);

        changeActionBarState(false, true, "");
        initViews(view);
        pollForUserAction();

        return view;
    }

    private void initViews(View view) {
        vFab = view.findViewById(R.id.fab_active);
        vFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performBindProcess(selectedDevice);
            }
        });
    }

    private void pollForUserAction() {
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setActionCheckerTimings(5, 10)
                .pollForUserAction(getContext(), Prefs.SEARCH_TOKEN.getValue(), selectedDevice.getDeviceId(), new CirrentService.UserActionCallback() {
                    @Override
                    public void onUserActionReceived(String message) {
                        vFab.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onUserActionFailed() {
                        int messageResId = R.string.action_checking_failed;
                        Toast.makeText(getContext(), messageResId, Toast.LENGTH_LONG).show();
                        showFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onUserActionPending() {
                        //nothing
                    }

                    @Override
                    public void onTokenExpired() {
                        refreshToken(new TokenRefresherCallback() {
                            @Override
                            public void onTokenSuccessfullyRefreshed() {
                                pollForUserAction();
                            }
                        });
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        Toast.makeText(getContext(), getString(R.string.cant_poll) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }
}
