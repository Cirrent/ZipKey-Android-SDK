package com.sampleapp.ui.fragment.demo;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.ui.fragment.BaseFullScreenFragment;

public class DemoZipkeyInfoFragment extends BaseFullScreenFragment
        implements View.OnClickListener {

    public static final String URL_ZIPKEY = "https://www.zipkey.net/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_demo_zipkey_info, container, false);

        changeActionBarState(true, false, "");
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        TextView vZipkeyUrl = (TextView) view.findViewById(R.id.url_zipkey_text);
        vZipkeyUrl.setPaintFlags(vZipkeyUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        vZipkeyUrl.setOnClickListener(this);

        view.findViewById(R.id.button_ok_cool).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.url_zipkey_text:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_ZIPKEY));
                startActivity(browserIntent);
                break;

            case R.id.button_ok_cool:
                getActivity().onBackPressed();
                break;
        }
    }

}
