package com.sampleapp.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.internal.net.model.DeviceDto;
import com.cirrent.cirrentsdk.net.model.Device;
import com.google.gson.Gson;
import com.sampleapp.DeviceBinder;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.analytics.AnalyticsService;
import com.sampleapp.net.requester.SearchTokenRequester;
import com.sampleapp.ui.FragmentListener;

public abstract class BaseFragment extends Fragment {
    private int numberOfTokenRenewals = 0;
    private AnalyticsService analyticsService;
    private FragmentListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentListener");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        analyticsService = new AnalyticsService(getContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void showFragment(Fragment fragment, boolean addToBackstack) {
        if (mListener != null) {
            mListener.showFragment(fragment, addToBackstack);
        }
    }

    public void changeActionBarState(boolean actionBarShouldBeHidden, boolean enableBackArrow, String title) {
        mListener.changeActionBarState(actionBarShouldBeHidden, enableBackArrow, title);
    }

    public void showToast(int resId, int duration) {
        mListener.showToast(resId, duration);
    }

    public void performBindProcess(final Device selectedDevice) {
        new DeviceBinder(getContext(), selectedDevice.getDeviceId(), new DeviceBinder.DeviceBinderCallback() {
            @Override
            public void onBound() {
                final String serializedSelectedDevice = new Gson().toJson(selectedDevice);
                final DeviceDto.ProviderKnownNetwork providerKnownNetwork = selectedDevice.getProviderKnownNetwork();
                if (providerKnownNetwork != null) {
                    getAnalyticsService().logEvent("real_flow", "zipkey_subscriber");
                    showFragment(SetupDeviceAutomaticallyFragment.newInstance(serializedSelectedDevice), false);
                } else {
                    getAnalyticsService().logEvent("real_flow", "zipkey_coverage");
                    showFragment(SetupDeviceManuallyFragment.newInstance(serializedSelectedDevice), false);
                }
            }

            @Override
            public void onFailure(CirrentException e) {
                Toast.makeText(getContext(), getString(R.string.failed_to_bind) + " Reason: " + e.getMessage(), Toast.LENGTH_LONG).show();
                showFragment(new HomeFragment(), false);
            }

            @Override
            public void onFailedRequestBindToken(String errorMessage) {
                Toast.makeText(getContext(), getString(R.string.failed_to_get_bind_token_please_try) + " Reason: " + errorMessage, Toast.LENGTH_LONG).show();
                showFragment(new HomeFragment(), false);
            }
        }).getBindTokenAndBindDevice(new SimpleProgressDialog(getContext(), getContext().getString(R.string.binding_device)));
    }

    public AnalyticsService getAnalyticsService() {
        return analyticsService;
    }

    public void refreshToken(final TokenRefresherCallback callback) {
        final int maxNumberOfAttempts = 2;
        if (numberOfTokenRenewals > maxNumberOfAttempts) {
            showFragment(new HomeFragment(), false);
            Toast.makeText(getContext(), R.string.invalid_search_token_too_many_attempts, Toast.LENGTH_SHORT).show();

            return;
        }

        new SearchTokenRequester(getContext(), Prefs.ENCODED_CREDENTIALS.getValue()) {
            @Override
            public void onSuccess(String searchToken) {
                callback.onTokenSuccessfullyRefreshed();
            }

            @Override
            public void onFailure(String errorMessage) {
                showFragment(new HomeFragment(), false);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }.doRequest(null);

        numberOfTokenRenewals += 1;
    }

    interface TokenRefresherCallback {
        void onTokenSuccessfullyRefreshed();
    }
}
