package com.sampleapp.ui.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.net.model.DeviceKnownNetwork;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.google.gson.Gson;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.ui.adapter.KnownNetworksAdapter;

import java.util.List;

public class KnownNetworksFragment extends BaseFragment {
    private static final String DEVICE_ID = "deviceId";

    private String deviceId;
    private List<DeviceKnownNetwork> knownNetworks;
    private ListView listKnownNetworks;
    private TextView textNoNetworks;
    private AlertDialog networkInfoDialog;
    private DeviceKnownNetwork removedNetwork;
    private SimpleProgressDialog knownNetworksProgressDialog;
    private Handler handler;
    private Runnable runnable;

    public KnownNetworksFragment() {
    }

    public static KnownNetworksFragment newInstance(String deviceId) {
        KnownNetworksFragment fragment = new KnownNetworksFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString(DEVICE_ID);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_known_networks, container, false);

        changeActionBarState(false, false, getString(R.string.known_networks));
        initViews(view);
        getKnownNetworks();

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_reset).setVisible(false);
        menu.findItem(R.id.action_networks).setVisible(false);
        menu.findItem(R.id.action_edit_name).setVisible(false);
        menu.findItem(R.id.action_perform_action).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPause() {
        super.onPause();
        CirrentService.getCirrentService().cancelAllTasks();
        stopHandlerCallbacks();
    }

    private void initViews(View view) {
        listKnownNetworks = (ListView) view.findViewById(R.id.list_known_networks);
        textNoNetworks = (TextView) view.findViewById(R.id.text_no_networks);

        setOnNetworkClickListener();
        setAddNetworkButtonClickListener(view);
    }

    private void setOnNetworkClickListener() {
        listKnownNetworks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                createNetworkInfoDialog(position);
                networkInfoDialog.show();
            }
        });
    }

    private void createNetworkInfoDialog(final int position) {
        final DeviceKnownNetwork selectedKnownNetwork = knownNetworks.get(position);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(selectedKnownNetwork.getDecodedSsid());
        dialogBuilder.setView(buildDialogBodyView(selectedKnownNetwork));
        dialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                networkInfoDialog.dismiss();
                deleteKnownNetwork(selectedKnownNetwork);
            }
        });
        dialogBuilder.setNeutralButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                networkInfoDialog.dismiss();
            }
        });

        networkInfoDialog = dialogBuilder.create();
    }

    @NonNull
    private View buildDialogBodyView(DeviceKnownNetwork selectedKnownNetwork) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogBodyView = inflater.inflate(R.layout.network_dialog_body, null);

        TextView textStatus = (TextView) dialogBodyView.findViewById(R.id.text_network_status);
        TextView textBssid = (TextView) dialogBodyView.findViewById(R.id.text_network_bssid);
        TextView textSecurity = (TextView) dialogBodyView.findViewById(R.id.text_network_security);

        textStatus.setText(selectedKnownNetwork.getStatus());
        textBssid.setText(selectedKnownNetwork.getBssid());
        textSecurity.setText(selectedKnownNetwork.getSecurity());

        return dialogBodyView;
    }

    private void deleteKnownNetwork(final DeviceKnownNetwork knownNetwork) {
        final String manageToken = Prefs.MANAGE_TOKEN.getValue();
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setProgressView(new SimpleProgressDialog(getContext(), getString(R.string.removing_network)))
                .deleteNetwork(getContext(), deviceId, manageToken, knownNetwork, new CirrentService.DeleteNetworkCallback() {
                    @Override
                    public void onNetworkDeleted() {
                        removedNetwork = knownNetwork;
                        getKnownNetworks();
                    }

                    @Override
                    public void onTokenExpired() {
                        String message = getString(R.string.search_token_expired);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        Toast.makeText(getContext(), getString(R.string.cant_delete_network) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }

    private void getKnownNetworks() {
        final int uptime = 1;
        final String manageToken = Prefs.MANAGE_TOKEN.getValue();
        startShowingProgress();
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .getDeviceKnownNetworks(getContext(), deviceId, uptime, manageToken, new CirrentService.DeviceKnownNetworksCallback() {
                    @Override
                    public void onDeviceKnownNetworksReceived(List<DeviceKnownNetwork> knownNetworks) {
                        if (knownNetworks != null && !knownNetworks.isEmpty()) {
                            if (removedNetwork != null) {
                                searchForRemovedNetwork(knownNetworks);
                            } else {
                                showKnownNetworks(knownNetworks);
                            }
                        } else {
                            knownNetworksProgressDialog.stopProgress();
                            textNoNetworks.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onTokenExpired() {
                        String message = getString(R.string.search_token_expired);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        Toast.makeText(getContext(), getString(R.string.cant_get_known_networks) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }

    private void startShowingProgress() {
        if (knownNetworksProgressDialog == null) {
            knownNetworksProgressDialog = new SimpleProgressDialog(getContext(), getString(R.string.updating_networks_status));
            knownNetworksProgressDialog.showProgress();
        } else {
            knownNetworksProgressDialog.showProgress();
        }
    }

    private void searchForRemovedNetwork(List<DeviceKnownNetwork> knownNetworks) {
        boolean removedNetworkFound = false;
        final String removedNetworkSsid = removedNetwork.getDecodedSsid();
        for (DeviceKnownNetwork knownNetwork : knownNetworks) {
            final String receivedNetworkSsid = knownNetwork.getDecodedSsid();
            if (removedNetworkSsid.equals(receivedNetworkSsid)) {
                removedNetworkFound = true;
                break;
            }
        }

        if (removedNetworkFound) {
            requestKnownNetworksAfterDelay();
        } else {
            showKnownNetworks(knownNetworks);
        }
    }

    private void requestKnownNetworksAfterDelay() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                getKnownNetworks();
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private void stopHandlerCallbacks() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void showKnownNetworks(List<DeviceKnownNetwork> knownNetworks) {
        knownNetworksProgressDialog.stopProgress();
        textNoNetworks.setVisibility(View.GONE);
        KnownNetworksFragment.this.knownNetworks = knownNetworks;
        listKnownNetworks.setAdapter(new KnownNetworksAdapter(getContext(), knownNetworks));
    }

    private void setAddNetworkButtonClickListener(View view) {
        view.findViewById(R.id.floating_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String serializedKnownNetworks = new Gson().toJson(knownNetworks);
                showFragment(SetupDeviceManuallyFragment.newInstance(deviceId, serializedKnownNetworks), false);
            }
        });
    }
}
