package com.sampleapp.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sampleapp.R;
import com.sampleapp.SimpleProgressDialog;
import com.sampleapp.Utils;
import com.sampleapp.net.requester.CreateAccountRequester;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class CreateAccountFragment extends BaseFragment implements View.OnClickListener {

    private static final String URL_PRIVACY_POLICY = "https://www.cirrent.com/privacy-policy";
    private static final String URL_TERMS_OF_SERVICE = "https://www.cirrent.com/terms-of-service/";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout companyNameLayout;
    private CheckBox checkBoxAgree;
    private TextView checkBoxText;
    private TextInputLayout checkBoxNotification;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_account, container, false);

        initViews(view);
        setupUiListenersAndValidators(view);
        setupClickableTexts();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeActionBarState(false, true, getString(R.string.create_an_account));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_create_account:
                if (isFieldsFilledCorrectly()) {

                    new CreateAccountRequester(
                            getContext(),
                            String.valueOf(firstNameLayout.getEditText().getText()),
                            String.valueOf(lastNameLayout.getEditText().getText()),
                            String.valueOf(companyNameLayout.getEditText().getText()),
                            String.valueOf(emailLayout.getEditText().getText()),
                            String.valueOf(passwordLayout.getEditText().getText())
                    ) {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), R.string.account_has_been_successfully_created, Toast.LENGTH_LONG).show();
                            getActivity().onBackPressed();
                        }

                    }.doRequest(new SimpleProgressDialog(getContext(), getString(R.string.account_is_being_created)));

                } else {
                    Utils.hideKeyboard(getActivity());
                }
        }
    }

    private void initViews(View view) {
        firstNameLayout = (TextInputLayout) view.findViewById(R.id.firstname_layout);
        lastNameLayout = (TextInputLayout) view.findViewById(R.id.lastname_layout);
        passwordLayout = (TextInputLayout) view.findViewById(R.id.pwd_layout);
        confirmPasswordLayout = (TextInputLayout) view.findViewById(R.id.pwd_confirm_layout);
        emailLayout = (TextInputLayout) view.findViewById(R.id.email_layout);
        companyNameLayout = (TextInputLayout) view.findViewById(R.id.company_layout);
        checkBoxAgree = (CheckBox) view.findViewById(R.id.checkbox_show_password);
        checkBoxText = (TextView) view.findViewById(R.id.checkbox_text);
        checkBoxNotification = (TextInputLayout) view.findViewById(R.id.checkbox_notification);
    }

    private void setupUiListenersAndValidators(View view) {
        view.findViewById(R.id.button_create_account).setOnClickListener(this);
        setupCheckingForFieldIsNotEmpty(firstNameLayout);
        setupCheckingForFieldIsNotEmpty(lastNameLayout);
        setupPasswordAndConfirmationChecking();
        setupEmailChecking();
        setupCheckingForFieldIsNotEmpty(companyNameLayout);
        setupUserAgreeValidation();
    }

    private void setupClickableTexts() {
        String text = getString(R.string.i_have_agree_to) + " "
                + getString(R.string.terms_of_service) + " "
                + getString(R.string.and) + " " + getString(R.string.privacy_policy);
        checkBoxText.setText(text);

        Map<String, ClickableSpan> links = new TreeMap<>();
        links.put(getString(R.string.terms_of_service), getClickableSpan(URL_TERMS_OF_SERVICE));
        links.put(getString(R.string.privacy_policy), getClickableSpan(URL_PRIVACY_POLICY));
        Utils.makeLinks(checkBoxText, links);
    }

    @NonNull
    private ClickableSpan getClickableSpan(final String uriString) {
        return new ClickableSpan() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uriString)));
            }
        };
    }

    // Validations listeners

    private void setupCheckingForFieldIsNotEmpty(final TextInputLayout inputLayout) {
        EditText editText = inputLayout.getEditText();
        if (editText == null) return;
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
                isFieldEmptyAndShowWarning(text, inputLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {
                isFieldEmptyAndShowWarning(s, inputLayout);
            }
        });
    }

    private void setupPasswordAndConfirmationChecking() {
        passwordLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isCorrectPasswordAndShowWarning();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        confirmPasswordLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isPasswordsMatchAndShowWarning();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupEmailChecking() {
        emailLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isValidEmailAndShowWarning();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupUserAgreeValidation() {
        checkBoxAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isUserAgreeAndShowWarning();
            }
        });
    }

    // Validations

    private boolean isFieldsFilledCorrectly() {
        List<Boolean> isFilledCorrectly = new ArrayList<>();

        isFilledCorrectly.add(!isFieldEmptyAndShowWarning(firstNameLayout));
        isFilledCorrectly.add(!isFieldEmptyAndShowWarning(lastNameLayout));
        isFilledCorrectly.add(isCorrectPasswordAndShowWarning());
        isFilledCorrectly.add(isPasswordsMatchAndShowWarning());
        isFilledCorrectly.add(isValidEmailAndShowWarning());
        isFilledCorrectly.add(!isFieldEmptyAndShowWarning(companyNameLayout));
        isFilledCorrectly.add(isUserAgreeAndShowWarning());

        return !isFilledCorrectly.contains(false);
    }

    private boolean isFieldEmptyAndShowWarning(TextInputLayout inputLayout) {
        return isFieldEmptyAndShowWarning(inputLayout.getEditText().getText(), inputLayout);
    }

    private boolean isFieldEmptyAndShowWarning(CharSequence text, TextInputLayout inputLayout) {
        boolean isEmpty;
        if (String.valueOf(text).trim().length() < 1) {
            inputLayout.setError(getString(R.string.fill_required_field));
            inputLayout.setErrorEnabled(true);
            isEmpty = true;
        } else {
            inputLayout.setErrorEnabled(false);
            isEmpty = false;
        }
        return isEmpty;
    }

    private boolean isCorrectPasswordAndShowWarning() {
        boolean isCorrectPassword;
        String text = String.valueOf(passwordLayout.getEditText().getText());
        if (PASSWORD_PATTERN.matcher(text).matches()) {
            passwordLayout.setErrorEnabled(false);
            isCorrectPassword = true;
        } else {
            passwordLayout.setError(getString(R.string.password_requirements));
            passwordLayout.setErrorEnabled(true);
            isCorrectPassword = false;
        }

        isPasswordsMatchAndShowWarning();

        return isCorrectPassword;
    }

    private boolean isPasswordsMatchAndShowWarning() {
        boolean isPasswordsMatch;
        String password = String.valueOf(passwordLayout.getEditText().getText());
        String confirmation = String.valueOf(confirmPasswordLayout.getEditText().getText());
        if (password.equals(confirmation)) {
            confirmPasswordLayout.setErrorEnabled(false);
            isPasswordsMatch = true;
        } else {
            confirmPasswordLayout.setError(getString(R.string.passwords_dont_match));
            confirmPasswordLayout.setErrorEnabled(true);
            isPasswordsMatch = false;
        }
        return isPasswordsMatch;
    }

    private boolean isValidEmailAndShowWarning() {
        boolean isEmailMatch;
        String text = String.valueOf(emailLayout.getEditText().getText());
        if (EMAIL_PATTERN.matcher(text).matches()) {
            emailLayout.setErrorEnabled(false);
            isEmailMatch = true;
        } else {
            emailLayout.setError(getString(R.string.enter_valid_email));
            emailLayout.setErrorEnabled(true);
            isEmailMatch = false;
        }
        return isEmailMatch;
    }

    private boolean isUserAgreeAndShowWarning() {
        boolean isChecked = checkBoxAgree.isChecked();
        if (isChecked) {
            checkBoxNotification.setVisibility(View.GONE);
        } else {
            checkBoxNotification.setVisibility(View.VISIBLE);
            checkBoxNotification.setErrorEnabled(true);
            checkBoxNotification.setError(getString(R.string.complete_this_mandatory_field));
        }
        return isChecked;
    }
}
