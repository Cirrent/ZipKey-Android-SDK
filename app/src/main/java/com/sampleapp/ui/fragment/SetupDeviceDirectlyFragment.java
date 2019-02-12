package com.sampleapp.ui.fragment;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.cirrent.cirrentsdk.net.model.WiFiNetwork;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SetupDeviceDirectlyFragment extends SetupDeviceBaseFragment {
    private Map<String, WiFiNetwork> ssidMap;
    private String selectedNetworkSsid;

    public Set<String> getSsidNames(List<WiFiNetwork> deviceCandidateNetworks) {
        ssidMap = new HashMap<>();
        for (WiFiNetwork candidateNetwork : deviceCandidateNetworks) {
            ssidMap.put(candidateNetwork.getDecodedSsid(), candidateNetwork);
        }
        return ssidMap.keySet();
    }

    public void preselectPrivateNetwork(String privateSsid, ArrayAdapter<String> spinnerAdapter, Spinner spinnerSsidList) {
        final int ssidPosition = spinnerAdapter.getPosition(privateSsid);
        if (!privateSsid.isEmpty() && ssidPosition != -1) {
            spinnerSsidList.setSelection(ssidPosition);
            selectedNetworkSsid = spinnerAdapter.getItem(ssidPosition);
        }
    }

    public String getSerializedHiddenNetwork(String hiddenSsid) {
        final WiFiNetwork hiddenNetwork = new WiFiNetwork();
        hiddenNetwork.setFlags(getSelectedSecurityType());
        hiddenNetwork.setSsid(hiddenSsid);
        return new Gson().toJson(hiddenNetwork);
    }

    public String getSerializedSelectedNetwork() {
        final WiFiNetwork selectedNetwork = getSsidMap().get(selectedNetworkSsid);
        return new Gson().toJson(selectedNetwork);
    }

    public String getSelectedNetworkSsid() {
        return selectedNetworkSsid;
    }

    public void setSelectedNetworkSsid(String selectedNetworkSsid) {
        this.selectedNetworkSsid = selectedNetworkSsid;
    }

    public Map<String, WiFiNetwork> getSsidMap() {
        return ssidMap;
    }
}
