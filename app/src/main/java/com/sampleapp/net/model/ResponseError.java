package com.sampleapp.net.model;

import com.google.gson.annotations.SerializedName;

public class ResponseError {
    @SerializedName("code")
    private String codeName;
    private String message;

    public ResponseError(String codeName, String message) {
        this.codeName = codeName;
        this.message = message;
    }

    public String getCodeName() {
        return codeName;
    }

    public String getMessage() {
        return message;
    }
}
