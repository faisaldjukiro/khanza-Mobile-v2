package com.faisal.rsas;

import com.faisal.rsas.model.Pasien;

import java.util.List;

public class PasienResponse {
    private String status;
    private String message;
    private List<Pasien> data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Pasien> getData() { return data; }
}