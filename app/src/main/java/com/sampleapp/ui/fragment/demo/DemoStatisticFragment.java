package com.sampleapp.ui.fragment.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.ui.activity.HomeActivity;
import com.sampleapp.ui.activity.StartActivity;
import com.sampleapp.ui.fragment.BaseFullScreenFragment;

public class DemoStatisticFragment extends BaseFullScreenFragment
        implements View.OnClickListener {

    private int connectionMethod;

    public static DemoStatisticFragment newInstance(int connectionMethod) {
        DemoStatisticFragment fragment = new DemoStatisticFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_demo_blue_statistic, container, false);

        changeActionBarState(true, false, "");

        view.findViewById(R.id.button_finish_walkthru).setOnClickListener(this);
        view.findViewById(R.id.button_learn_more).setOnClickListener(this);

        highlightTableRow(view);

        return view;
    }

    private void highlightTableRow(View view) {
        LinearLayout vTable = (LinearLayout) view.findViewById(R.id.table);
        int color = ContextCompat.getColor(getContext(), R.color.light_blue);
        int rowIndex = -1;
        switch (connectionMethod) {
            case DemoStartFragment.CONNECTION_METHOD_1:
                rowIndex = 0;
                break;
            case DemoStartFragment.CONNECTION_METHOD_2:
                rowIndex = 2;
                break;
            case DemoStartFragment.CONNECTION_METHOD_3:
                rowIndex = 4;
                break;
        }
        vTable.getChildAt(rowIndex).setBackgroundColor(color);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_finish_walkthru:
                Intent intent;
                if (Prefs.SEARCH_TOKEN.exists()) {
                    intent = new Intent(getActivity(), HomeActivity.class);
                } else {
                    intent = new Intent(getActivity(), StartActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case R.id.button_learn_more:
                showFragment(new DemoAcmeZipkeyTalkFragment(), true);
                break;
        }
    }

}
