package com.sampleapp.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cirrent.cirrentsdk.net.model.Device;
import com.sampleapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.regex.Pattern;

public class NearbyDeviceAdapter extends BaseAdapter {
    private static final Pattern PATTERN_NUMBER = Pattern.compile("^\\d+$");
    private Context context;
    private List<Device> deviceList;
    private Device selectedDevice;

    public NearbyDeviceAdapter(Context context, List<Device> deviceList) {
        this.deviceList = deviceList;
        this.context = context;
    }

    public void processClickOnItem(int position) {
        selectedDevice = deviceList.get(position);

        notifyDataSetChanged();
    }

    public Device getSelectedDevice() {
        return selectedDevice;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
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
            convertView = inflater.inflate(R.layout.item_found_device, parent, false);

            viewHolder = new DeviceViewHolder();
            viewHolder.isChecked = (ImageView) convertView.findViewById(R.id.is_checked);
            viewHolder.deviceProvider = (ImageView) convertView.findViewById(R.id.provider_logo);
            viewHolder.deviceId = (TextView) convertView.findViewById(R.id.text_id_device);
            viewHolder.deviceType = (TextView) convertView.findViewById(R.id.text_type_device);
            viewHolder.deviceImage = (ImageView) convertView.findViewById(R.id.img_device);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeviceViewHolder) convertView.getTag();
        }

        final Device device = deviceList.get(position);
        viewHolder.deviceId.setText(device.getDeviceId());
        viewHolder.deviceType.setText(device.getDeviceType());

        boolean isSelected = device.equals(selectedDevice);
        if (isSelected) {
            viewHolder.isChecked.setImageResource(R.drawable.circle_selected);
            viewHolder.deviceId.setTextColor(ContextCompat.getColor(context, R.color.item_selected));
            viewHolder.deviceType.setTextColor(ContextCompat.getColor(context, R.color.item_selected));
        } else {
            viewHolder.isChecked.setImageResource(R.drawable.circle_unselected);
            viewHolder.deviceId.setTextColor(ContextCompat.getColor(context, R.color.black));
            viewHolder.deviceType.setTextColor(ContextCompat.getColor(context, R.color.item_unselected));
        }

        loadDeviceImage(viewHolder, device);

        return convertView;
    }

    private void loadDeviceImage(DeviceViewHolder viewHolder, Device device) {
        final String imageUrlOrResourceId = device.getImageURL();
        if (imageUrlOrResourceId == null || imageUrlOrResourceId.length() < 1) return;

        if (isNaturalNumber(imageUrlOrResourceId)) {
            viewHolder.deviceImage.setImageResource(Integer.valueOf(imageUrlOrResourceId));
        } else {
            Picasso.with(context).load(imageUrlOrResourceId).into(viewHolder.deviceImage);
        }
    }

    private boolean isNaturalNumber(String string) {
        return PATTERN_NUMBER.matcher(string).matches();
    }

    private static class DeviceViewHolder {
        private ImageView isChecked;
        private ImageView deviceProvider;
        private TextView deviceId;
        private TextView deviceType;
        private ImageView deviceImage;
    }

}
