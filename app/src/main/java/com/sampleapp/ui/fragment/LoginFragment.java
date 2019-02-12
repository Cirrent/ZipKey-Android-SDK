package com.sampleapp.ui.fragment;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sampleapp.Prefs;
import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.Utils;
import com.sampleapp.net.requester.LoginRequester;
import com.sampleapp.net.requester.SearchTokenRequester;
import com.sampleapp.ui.activity.HomeActivity;

public class LoginFragment extends BaseFragment implements View.OnClickListener {

    public static final String CIRRENT_FORGOTPASSWORD = "https://go.cirrent.com/forgotpassword";
    private EditText fieldLogin;
    private EditText fieldPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        fieldLogin = (EditText) view.findViewById(R.id.edit_login);
        fieldPassword = (EditText) view.findViewById(R.id.edit_password);
        CheckBox checkBoxShowPassword = (CheckBox) view.findViewById(R.id.checkbox_show_password);
        view.findViewById(R.id.img_back_arrow).setOnClickListener(this);
        view.findViewById(R.id.button_sign_in).setOnClickListener(this);
        TextView vForgotPassword = (TextView) view.findViewById(R.id.forgot_password);
        vForgotPassword.setPaintFlags(vForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        vForgotPassword.setOnClickListener(this);

        setupShowPassCheckboxListener(checkBoxShowPassword);
    }

    private void setupShowPassCheckboxListener(CheckBox checkBoxShowPassword) {
        checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fieldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    fieldPassword.setSelection(fieldPassword.getText().length());
                } else {
                    fieldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    fieldPassword.setSelection(fieldPassword.getText().length());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back_arrow:
                getActivity().onBackPressed();
                break;
            case R.id.button_sign_in:
                final String username = String.valueOf(fieldLogin.getText());
                final String password = String.valueOf(fieldPassword.getText());
                final String encodedCredentials = Utils.encodeCredentialsToBase64(username, password);

                if (Prefs.SEARCH_TOKEN.exists()) {
                    Utils.hideKeyboard(getActivity());
                    getActivity().startActivity(new Intent(getContext(), HomeActivity.class));
                } else {
                    login(username, password, encodedCredentials);
                }
                break;
            case R.id.forgot_password:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CIRRENT_FORGOTPASSWORD));
                startActivity(browserIntent);
                break;
        }
    }

    private void login(final String username, final String password, final String encodedCredentials) {
        final SimpleProgressDialog progressDialog = new SimpleProgressDialog(getContext(), getString(R.string.logging_in));
        new LoginRequester(getContext(), username, password) {
            @Override
            public void onSuccess(String accountId) {
                Prefs.ACCOUNT_ID.setValue(accountId);
                getSearchToken(encodedCredentials, progressDialog);
            }
        }.doRequest(progressDialog);
    }

    private void getSearchToken(final String encodedCredentials, SimpleProgressDialog progressDialog) {
        new SearchTokenRequester(getContext(), encodedCredentials) {
            @Override
            public void onSuccess(String searchToken) {
                createAppId();
                Prefs.ENCODED_CREDENTIALS.setValue(encodedCredentials);
                Prefs.SOFT_AP_SSID.setValue("ca-softap");
                Utils.hideKeyboard(getActivity());
                Intent intent = new Intent(getContext(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), R.string.such_login_and_password_not_exist, Toast.LENGTH_LONG).show();
            }
        }.doRequest(progressDialog);
    }

    private void createAppId() {
        if (!Prefs.APP_ID.exists()) {
            Prefs.APP_ID.setValue(String.valueOf(fieldLogin.getText()));
        }
    }
}
