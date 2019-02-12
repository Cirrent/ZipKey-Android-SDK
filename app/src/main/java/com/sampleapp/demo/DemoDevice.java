package com.sampleapp.demo;

import java.io.Serializable;

public class DemoDevice implements Serializable {
    private int providerLogo;
    private String deviceId;
    private String deviceName;
    private String imageURL;

    public int getProviderLogo() {
        return providerLogo;
    }

    public void setProviderLogo(int providerLogo) {
        this.providerLogo = providerLogo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

}
