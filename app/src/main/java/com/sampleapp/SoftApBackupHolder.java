package com.sampleapp;

import com.cirrent.cirrentsdk.net.model.WiFiNetwork;

public class SoftApBackupHolder {
    private String deviceId;
    private String credentialsId;
    private WiFiNetwork selectedNetwork;

    public SoftApBackupHolder(String deviceId, String credentialsId, WiFiNetwork selectedNetwork) {
        this.deviceId = deviceId;
        this.credentialsId = credentialsId;
        this.selectedNetwork = selectedNetwork;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public WiFiNetwork getSelectedNetwork() {
        return selectedNetwork;
    }
}
