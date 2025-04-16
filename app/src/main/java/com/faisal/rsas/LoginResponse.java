package com.faisal.rsas;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}
