package com.sampleapp.ui.fragment.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sampleapp.R;
import com.sampleapp.ui.fragment.BaseFullScreenFragment;

public class DemoAcmeRememberCredentialsFragment extends BaseFullScreenFragment
        implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_demo_remember_credentials, container, false);

        changeActionBarState(true, false, "");

        view.findViewById(R.id.zipkey_logo).setOnClickListener(this);
        view.findViewById(R.id.button_ok_cool).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zipkey_logo:
                showFragment(new DemoZipkeyInfoFragment(), true);
                break;

            case R.id.button_ok_cool:
                getActivity().onBackPressed();
                break;
        }
    }

}
