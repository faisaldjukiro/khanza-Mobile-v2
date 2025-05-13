package com.faisal.rsas.model;

public class JenisBerkas {
    private final String kode;
    private final String nama;

    public JenisBerkas(String kode, String nama) {
        this.kode = kode;
        this.nama = nama;
    }

    public String getKode() {
        return kode;
    }

    public String getNama() {
        return nama;
    }

    @Override
    public String toString() {
        return nama;
    }
}
