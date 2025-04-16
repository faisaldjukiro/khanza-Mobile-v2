package com.faisal.rsas.api;

import com.faisal.rsas.LoginResponse;
import com.faisal.rsas.PasienResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> loginUser(
            @Field("username") String username,
            @Field("password") String password
    );

    @GET("api/pasien")
    Call<PasienResponse> getPasien(@Header("Authorization") String token);

    @GET("api/validasitoken")
    Call<ResponseBody> validateToken(@Header("Authorization") String token);
}
