package com.sampleapp.ui.fragment;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.ui.activity.demo.DemoActivity;

public class StartFragment extends BaseFragment implements View.OnClickListener {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_start, container, false);

        setupViews(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeActionBarState(true, false, "");
    }

    private void setupViews(View view) {
        view.findViewById(R.id.button_sign_in).setOnClickListener(this);
        TextView vCreateAccount = (TextView) view.findViewById(R.id.button_create_account);
        vCreateAccount.setPaintFlags(vCreateAccount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        vCreateAccount.setOnClickListener(this);
        view.findViewById(R.id.button_show_walkthru).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                showFragment(new LoginFragment(), true);
                break;
            case R.id.button_create_account:
                showFragment(new CreateAccountFragment(), true);
                break;
            case R.id.button_show_walkthru:
                startActivity(new Intent(getActivity(), DemoActivity.class));
                break;
        }
    }

}
