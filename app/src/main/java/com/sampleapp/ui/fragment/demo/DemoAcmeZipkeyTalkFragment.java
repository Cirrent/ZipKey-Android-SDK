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

public class DemoAcmeZipkeyTalkFragment extends BaseFullScreenFragment
        implements View.OnClickListener {

    public static final String URL_COMCAST_ZIPKEY = "http://www.xfinity.com";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutId = R.layout.fragment_demo_acme_xfinity_zipkey_talk;
        View view = inflater.inflate(layoutId, container, false);

        changeActionBarState(true, false, "");

        TextView vZipkeyUrl = (TextView) view.findViewById(R.id.url_zipkey_text);
        vZipkeyUrl.setPaintFlags(vZipkeyUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        view.findViewById(R.id.url_zipkey).setOnClickListener(this);
        view.findViewById(R.id.button_ok_cool).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.url_zipkey:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_COMCAST_ZIPKEY));
                startActivity(browserIntent);
                break;

            case R.id.button_ok_cool:
                getActivity().onBackPressed();
                break;
        }
    }

}
