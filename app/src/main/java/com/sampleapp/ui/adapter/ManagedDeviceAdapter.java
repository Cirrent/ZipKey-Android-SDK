package com.sampleapp.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.DeviceKnownNetwork;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.Utils;
import com.sampleapp.net.model.ManagedDeviceList;
import com.sampleapp.net.requester.ResetManagedDeviceRequester;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import co.stkotok.swipetodelete.STDAdapterHelper;
import co.stkotok.swipetodelete.STDInterface;

public class ManagedDeviceAdapter
        extends RecyclerView.Adapter<ManagedDeviceAdapter.Holder>
        implements STDInterface {

    private final Context context;
    private final STDAdapterHelper<ManagedDeviceAdapter> stdHelper;

    private OnClick itemOnClickListener;
    private DeviceResettedListener deviceResettedListener;
    private List<ManagedDeviceList.ProductCloudDevice> managedDevices = new ArrayList<>();

    public ManagedDeviceAdapter(Context context, int backgroundColor) {
        this.context = context;
        stdHelper = new STDAdapterHelper<>(3000, backgroundColor, this);
    }

    public void setOnItemClickListener(OnClick itemOnClickListener) {
        this.itemOnClickListener = itemOnClickListener;
    }

    public void setOnDeviceResettedListener(DeviceResettedListener listener) {
        this.deviceResettedListener = listener;
    }

    public void setDevices(List<ManagedDeviceList.ProductCloudDevice> managedDevices) {
        this.managedDevices = managedDevices;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return managedDevices.size();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new Holder((inflater.inflate(R.layout.item_managed_device, parent, false)));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        stdHelper.onBindViewHolder(holder, position);

        final ManagedDeviceList.ProductCloudDevice device = managedDevices.get(position);

        final String deviceId = device.getDeviceId();
        setIsActiveStatus(deviceId, holder.isActive);
        Picasso.with(context).load(device.getBrandLogoUrl()).into(holder.brandLogo);
        holder.deviceId.setText(Utils.getFriendlyName(deviceId));
        holder.deviceType.setText(device.getDeviceTypeName());
        loadDeviceImage(device, holder.deviceImage);

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemOnClickListener.onListItem(device);
            }
        });
    }

    @Override
    public void removingFromItems(int position) {
        ManagedDeviceList.ProductCloudDevice device = managedDevices.get(position);
        resetManagedDevice(device, position);
    }

    @Override
    public List getItems() {
        return managedDevices;
    }

    @Override
    public STDAdapterHelper getSTDAdapterHelper() {
        return stdHelper;
    }

    private void setIsActiveStatus(final String deviceId, final ImageView isActiveView) {
        final String manageToken = Prefs.MANAGE_TOKEN.getValue();

        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .getDeviceKnownNetworks(context, deviceId, manageToken, new CirrentService.DeviceKnownNetworksCallback() {
                    @Override
                    public void onDeviceKnownNetworksReceived(List<DeviceKnownNetwork> knownNetworks) {
                        if (knownNetworks != null && !knownNetworks.isEmpty()) {
                            boolean isActive = isDeviceActive(knownNetworks);
                            isActiveView.setImageResource(isActive ? R.drawable.circle_green : R.drawable.circle_unselected);
                        }
                    }

                    private boolean isDeviceActive(List<DeviceKnownNetwork> networks) {
                        for (DeviceKnownNetwork network : networks) {
                            final String statusJoined = "JOINED";
                            final String source = "NetworkConfig";
                            if (statusJoined.equals(network.getStatus()) && !source.equals(network.getSource())) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void onTokenExpired() {
                        String message = context.getString(R.string.search_token_expired);
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        String message = context.getString(R.string.cant_get_known_networks) + " "
                                + context.getString(R.string.reason_colon) + " " + e.getMessage();
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }

    private void loadDeviceImage(ManagedDeviceList.ProductCloudDevice device, ImageView deviceImageView) {
        final String imageURL = device.getImageUrl();
        if (imageURL != null) {
            Picasso.with(context).load(imageURL).fit().centerInside().into(deviceImageView);
        }
    }

    private void resetManagedDevice(final ManagedDeviceList.ProductCloudDevice device, final int position) {
        final String deviceId = device.getDeviceId();
        final String manageToken = Prefs.MANAGE_TOKEN.getValue();
        //----- SDK call ------------
        String string = context.getString(R.string.resetting_device_cirrent_cloud);
        CirrentService
                .getCirrentService()
                .setProgressView(new SimpleProgressDialog(context, string))
                .resetDevice(context, deviceId, manageToken, new CirrentService.ResetDeviceCallback() {
                    @Override
                    public void onDeviceReset() {
                        resetDeviceOnProductSide(deviceId);
                    }

                    @Override
                    public void onTokenExpired() {
                        putAgainInList(position, device);
                        deviceResettedListener.onResetted(deviceId, false);
                        Toast.makeText(context, R.string.manage_expired, Toast.LENGTH_SHORT).show();
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        putAgainInList(position, device);
                        deviceResettedListener.onResetted(deviceId, false);
                        Toast.makeText(context, context.getString(R.string.cant_reset) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }

    private void resetDeviceOnProductSide(final String deviceId) {
        new ResetManagedDeviceRequester(context, deviceId, Prefs.ENCODED_CREDENTIALS.getValue()) {
            @Override
            public void onSuccess() {
                deviceResettedListener.onResetted(deviceId, true);
            }

            @Override
            public void onFailure(String error, int statusCode, String errorBody) {
                deviceResettedListener.onResetted(deviceId, false);
                super.onFailure(error, statusCode, errorBody);
            }
        }.doRequest(new SimpleProgressDialog(context, context.getString(R.string.resetting_device_product_cloud)));
    }

    private void putAgainInList(int position, ManagedDeviceList.ProductCloudDevice device) {
        managedDevices.add(position, device);
        notifyItemInserted(position);
    }

    public interface OnClick {
        void onListItem(ManagedDeviceList.ProductCloudDevice device);
    }

    public interface DeviceResettedListener {
        void onResetted(String deviceId, boolean isSuccessfull);
    }


    static class Holder extends STDAdapterHelper.VHolder {
        private ImageView isActive;
        private ImageView brandLogo;
        private TextView deviceId;
        private TextView deviceType;
        private ImageView deviceImage;

        Holder(View itemView) {
            super(itemView, itemView.findViewById(R.id.item_main_layout), itemView.findViewById(R.id.undo));
            isActive = (ImageView) itemView.findViewById(R.id.is_active);
            brandLogo = (ImageView) itemView.findViewById(R.id.provider_logo);
            deviceId = (TextView) itemView.findViewById(R.id.text_id_device);
            deviceType = (TextView) itemView.findViewById(R.id.text_type_device);
            deviceImage = (ImageView) itemView.findViewById(R.id.img_device);
        }
    }

}
