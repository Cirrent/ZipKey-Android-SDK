package com.sampleapp.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cirrent.cirrentsdk.CirrentException;
import com.cirrent.cirrentsdk.CommonErrorCallback;
import com.cirrent.cirrentsdk.service.CirrentService;
import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.Utils;
import com.sampleapp.net.requester.ResetManagedDeviceRequester;
import com.sampleapp.ui.fragment.DeviceInfoFragment;
import com.sampleapp.ui.fragment.KnownNetworksFragment;
import com.sampleapp.ui.fragment.SetupDeviceManuallyFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DeviceInfoActivity extends BaseActivity {

    public static final String DEVICE_ID = "deviceId";
    public static final String DEVICE_IMAGE_URL = "deviceImageUrl";
    public static final int REMOVE_REQUEST_CODE = 111;

    private String deviceId;
    private String deviceImgUrl;
    private String actionText;
    private AlertDialog resetDeviceDialog;
    private AlertDialog renameDeviceDialog;
    private AlertDialog productActionDialog;
    private ActionBar supportActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        setupActionBar();

        Intent intent = getIntent();
        deviceId = intent.getStringExtra(DEVICE_ID);
        deviceImgUrl = intent.getStringExtra(DEVICE_IMAGE_URL);

        showFragment(DeviceInfoFragment.newInstance(deviceId, deviceImgUrl), false);
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_info_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment knownNetworksFragment = supportFragmentManager.findFragmentByTag(KnownNetworksFragment.class.getName());
        Fragment setupManuallyFragment = supportFragmentManager.findFragmentByTag(SetupDeviceManuallyFragment.class.getName());
        if (knownNetworksFragment != null && knownNetworksFragment.isVisible()) {
            showFragment(new DeviceInfoFragment(), false);
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (setupManuallyFragment != null && setupManuallyFragment.isVisible()) {
            showFragment(KnownNetworksFragment.newInstance(deviceId), false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                createResetDeviceDialog();
                resetDeviceDialog.show();

                return true;
            case R.id.action_networks:
                showFragment(KnownNetworksFragment.newInstance(deviceId), true);

                return true;
            case R.id.action_edit_name:
                createRenameDeviceDialog();
                renameDeviceDialog.show();

                return true;
            case R.id.action_perform_action:
                createProductActionDialog();
                productActionDialog.show();

                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createResetDeviceDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.remove_device_dialog_title);
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetDeviceDialog.dismiss();
                resetManagedDevice();
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetDeviceDialog.dismiss();
            }
        });

        resetDeviceDialog = dialogBuilder.create();
    }

    private void createRenameDeviceDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.name_your_product);
        dialogBuilder.setView(createDialogBodyView());
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editRenameDevice = (EditText) renameDeviceDialog.findViewById(R.id.edit_device_name);

                if (editRenameDevice != null && !String.valueOf(editRenameDevice.getText()).isEmpty()) {
                    Utils.setFriendlyName(deviceId, String.valueOf(editRenameDevice.getText()));
                    showFragment(DeviceInfoFragment.newInstance(deviceId, deviceImgUrl), false);
                    renameDeviceDialog.dismiss();
                } else {
                    Toast.makeText(DeviceInfoActivity.this, R.string.name_cant_be_empty, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                renameDeviceDialog.dismiss();
            }
        });

        renameDeviceDialog = dialogBuilder.create();
    }

    private void createProductActionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.enter_product_action);
        dialogBuilder.setView(createDialogBodyView());
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editProductAction = (EditText) productActionDialog.findViewById(R.id.edit_device_name);

                if (editProductAction != null && !String.valueOf(editProductAction.getText()).isEmpty()) {
                    actionText = String.valueOf(editProductAction.getText());
                    productActionDialog.dismiss();
                    performAction(actionText);

                } else {
                    Toast.makeText(DeviceInfoActivity.this, R.string.name_cant_be_empty, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                productActionDialog.dismiss();
            }
        });

        productActionDialog = dialogBuilder.create();
    }
    private View createDialogBodyView() {
        LayoutInflater inflater = LayoutInflater.from(this);

        return inflater.inflate(R.layout.dialog_name_device_layout, null);
    }

    private void resetManagedDevice() {
        final String manageToken = Prefs.MANAGE_TOKEN.getValue();
        //----- SDK call ------------
        String msg = getString(R.string.resetting_device_cirrent_cloud);
        CirrentService
                .getCirrentService()
                .setProgressView(new SimpleProgressDialog(this, msg))
                .resetDevice(this, deviceId, manageToken, new CirrentService.ResetDeviceCallback() {
                    @Override
                    public void onDeviceReset() {
                        resetDeviceOnProductSide();
                    }

                    @Override
                    public void onTokenExpired() {
                        int message = R.string.manage_expired;
                        Toast.makeText(DeviceInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        Toast.makeText(DeviceInfoActivity.this, getString(R.string.cant_reset) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }

    private void resetDeviceOnProductSide() {
        new ResetManagedDeviceRequester(this, deviceId, Prefs.ENCODED_CREDENTIALS.getValue()) {
            @Override
            public void onSuccess() {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error, int statusCode, String errorBody) {
                if (statusCode == 404) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    super.onFailure(error, statusCode, errorBody);
                }
            }
        }.doRequest(new SimpleProgressDialog(this, getString(R.string.resetting_device_product_cloud)));
    }

    private void performAction(String actionText) {
        final String manageToken = Prefs.MANAGE_TOKEN.getValue();
        //----- SDK call ------------
        CirrentService
                .getCirrentService()
                .setProgressView(new SimpleProgressDialog(this, "Sending action"))
                .performAction(this, deviceId, actionText, manageToken, new CirrentService.ProductActionCallback() {
                    @Override
                    public void onProductActionSent() {
                        Toast.makeText(DeviceInfoActivity.this, R.string.action_sent, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTokenExpired() {
                        int message = R.string.manage_expired;
                        Toast.makeText(DeviceInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }, new CommonErrorCallback() {
                    @Override
                    public void onFailure(CirrentException e) {
                        Toast.makeText(DeviceInfoActivity.this, getString(R.string.cant_perform_action) + " Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        //---------------------------
    }

    @Override
    public void changeActionBarState(boolean hideActionBar, boolean enableBackArrow, String title) {
        if (hideActionBar) {
            supportActionBar.hide();
            return;
        } else {
            supportActionBar.show();
        }

        if (title.isEmpty()) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        } else {
            supportActionBar.setDisplayShowTitleEnabled(true);
            supportActionBar.setTitle(title);
        }
    }
}
