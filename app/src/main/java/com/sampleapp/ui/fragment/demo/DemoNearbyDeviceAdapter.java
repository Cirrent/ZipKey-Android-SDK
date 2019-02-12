package com.sampleapp.ui.fragment.demo;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sampleapp.R;
import com.sampleapp.demo.DemoDevice;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DemoNearbyDeviceAdapter extends BaseAdapter {
    private static final Pattern PATTERN_NUMBER = Pattern.compile("^\\d+$");
    private Context context;
    private List<DemoDevice> deviceList;
    private Set<Integer> selectedDevicesSet = new HashSet<>();
    private OnItemClick onItemClickListener;

    public DemoNearbyDeviceAdapter(Context context, List<DemoDevice> deviceList) {
        this.deviceList = deviceList;
        this.context = context;
    }

    public ArrayList<DemoDevice> getSelectedDevices() {
        ArrayList<DemoDevice> devices = new ArrayList<>();
        for (int i = 0; i < deviceList.size(); i++) {
            if (selectedDevicesSet.contains(i)) {
                devices.add(deviceList.get(i));
            }
        }
        return devices;
    }

    public boolean isAnyDeviceSelected() {
        return selectedDevicesSet.isEmpty();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final DeviceViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_found_device, parent, false);

            viewHolder = new DeviceViewHolder();
            viewHolder.layout = convertView.findViewById(R.id.layout_item_found);
            viewHolder.isChecked = (ImageView) convertView.findViewById(R.id.is_checked);
            viewHolder.providerLogo = (ImageView) convertView.findViewById(R.id.provider_logo);
            viewHolder.deviceId = (TextView) convertView.findViewById(R.id.text_id_device);
            viewHolder.deviceType = (TextView) convertView.findViewById(R.id.text_type_device);
            viewHolder.deviceImage = (ImageView) convertView.findViewById(R.id.img_device);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeviceViewHolder) convertView.getTag();
        }

        final DemoDevice device = deviceList.get(position);

        viewHolder.providerLogo.setImageResource(device.getProviderLogo());
        viewHolder.deviceId.setText(device.getDeviceId());
        viewHolder.deviceType.setText(device.getDeviceName());

        boolean isSelected = selectedDevicesSet.contains(position);
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

        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDevicesSet.contains(position)) {
                    selectedDevicesSet.remove(position);
                } else {
                    selectedDevicesSet.add(position);
                }
                notifyDataSetChanged();

                onItemClickListener.itemSelectionChanged();
            }
        });

        viewHolder.deviceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.processDeviceInfo(device);
            }
        });

        return convertView;
    }

    public void setOnItemClickListener(OnItemClick onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private void loadDeviceImage(DeviceViewHolder viewHolder, DemoDevice device) {
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

    public interface OnItemClick {
        void itemSelectionChanged();

        void processDeviceInfo(DemoDevice position);
    }

    private static class DeviceViewHolder {
        private View layout;
        private ImageView isChecked;
        private ImageView providerLogo;
        private TextView deviceId;
        private TextView deviceType;
        private ImageView deviceImage;
    }

}
