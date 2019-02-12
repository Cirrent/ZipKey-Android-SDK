package com.sampleapp.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ManagedDeviceList {
    @SerializedName("manage_token")
    private String manageToken;
    private List<ProductCloudDevice> devices = new ArrayList<>();

    public String getManageToken() {
        return manageToken;
    }

    public void setManageToken(String manageToken) {
        this.manageToken = manageToken;
    }

    public List<ProductCloudDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<ProductCloudDevice> devices) {
        this.devices = devices;
    }

    public class ProductCloudDevice {
        @SerializedName("cirrent_device_id")
        private String deviceId;
        @SerializedName("friendly_name")
        private String friendlyName;
        @SerializedName("device_type_name")
        private String deviceTypeName;
        @SerializedName("device_type_image")
        private String imageUrl;
        @SerializedName("brand_logo")
        private String brandLogoUrl;

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public void setFriendlyName(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getDeviceTypeName() {
            return deviceTypeName;
        }

        public void setDeviceTypeName(String deviceTypeName) {
            this.deviceTypeName = deviceTypeName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getBrandLogoUrl() {
            return brandLogoUrl;
        }

        public void setBrandLogoUrl(String brandLogoUrl) {
            this.brandLogoUrl = brandLogoUrl;
        }
    }
}
