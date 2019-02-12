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
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.Utils;

import java.util.Collections;
import java.util.List;

public class FindDeviceFragment extends BaseFullScreenFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_find_device_loading, container, false);
        Utils.startConstantVerticalViewAnimation(view.findViewById(R.id.logo));

        changeActionBarState(true, false, "");
        findDevices();

        return view;
    }

    private void findDevices() {
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .findDevices(
                        getContext(),
                        Prefs.SEARCH_TOKEN.getValue(),
                        new CirrentService.DeviceInfoCallback() {
                            @Override
                            public void onDevicesFound(final List<Device> nearbyDevices) {
                                final String serializedList = new Gson().toJson(nearbyDevices);
                                showFragment(AddDeviceFragment.newInstance(serializedList), false);
                            }

                            @Override
                            public void onWifiDisabled() {
                                Toast.makeText(getContext(), R.string.turn_on_wifi, Toast.LENGTH_SHORT).show();
                                showFragment(new HomeFragment(), false);
                            }

                            @Override
                            public void onTokenExpired() {
                                refreshToken(new TokenRefresherCallback() {
                                    @Override
                                    public void onTokenSuccessfullyRefreshed() {
                                        findDevices();
                                    }
                                });
                            }

                            @Override
                            public void onEnvironmentGatheringNotInitialized() {
                                Toast.makeText(getContext(), R.string.gathering_not_initialized, Toast.LENGTH_SHORT).show();
                            }
                        }, new CommonErrorCallback() {
                            @Override
                            public void onFailure(CirrentException e) {
                                Toast.makeText(getContext(), "Can't find devices. Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                showFragment(AddDeviceFragment.newInstance(new Gson().toJson(Collections.EMPTY_LIST)), false);
                            }
                        });
        //---------------------------
    }
}
