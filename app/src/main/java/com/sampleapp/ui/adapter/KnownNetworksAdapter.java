package com.sampleapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cirrent.cirrentsdk.net.model.DeviceKnownNetwork;
import com.sampleapp.R;

import java.util.List;

public class KnownNetworksAdapter extends BaseAdapter {
    private Context context;
    private List<DeviceKnownNetwork> knownNetworks;

    public KnownNetworksAdapter(Context context,
                                List<DeviceKnownNetwork> knownNetworks) {
        this.knownNetworks = knownNetworks;
        this.context = context;
    }

    @Override
    public int getCount() {
        return knownNetworks.size();
    }

    @Override
    public Object getItem(int position) {
        return knownNetworks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DeviceViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_network, parent, false);

            viewHolder = new DeviceViewHolder();
            viewHolder.ssid = (TextView) convertView.findViewById(R.id.text_ssid);
            viewHolder.connected = (TextView) convertView.findViewById(R.id.text_connected);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeviceViewHolder) convertView.getTag();
        }

        final DeviceKnownNetwork knownNetwork = knownNetworks.get(position);

        viewHolder.ssid.setText(knownNetwork.getDecodedSsid());

        final String statusConnected = "JOINED";
        if (statusConnected.equals(knownNetwork.getStatus())) {
            viewHolder.connected.setVisibility(View.VISIBLE);
        }

        return convertView;
    }


    private static class DeviceViewHolder {
        private TextView ssid;
        private TextView connected;
    }
}
