package com.faisal.rsas.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("token")
    private String token;

    @SerializedName("data")
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        @SerializedName("user")
        private User user;

        public User getUser() {
            return user;
        }
    }

    public class User {
        @SerializedName("username")
        private String username;

        @SerializedName("nama")
        private String nama;

        public String getUsername() {
            return username;
        }

        public String getNama() {
            return nama;
        }
    }
}
