package com.sampleapp.net.model;

import com.google.gson.annotations.SerializedName;

public class AccountHolder {

    @SerializedName("first_name")
    private final String firstName;
    @SerializedName("last_name")
    private final String lastName;
    @SerializedName("company")
    private final String companyName;
    @SerializedName("email")
    private final String email;
    @SerializedName("password")
    private final String password;

    public AccountHolder(String firstName, String lastName, String companyName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.companyName = companyName;
        this.email = email;
        this.password = password;
    }

}
