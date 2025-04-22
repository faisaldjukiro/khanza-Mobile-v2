package com.faisal.rsas.response;

import com.faisal.rsas.item.BerkasItem;

import java.util.List;

public class BerkasResponse {
    private String status;
    private String message;
    private List<BerkasItem> data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<BerkasItem> getData() {
        return data;
    }
}