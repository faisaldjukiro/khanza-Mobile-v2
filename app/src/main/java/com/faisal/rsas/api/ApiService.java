package com.faisal.rsas.api;

import com.faisal.rsas.model.DirectionsResponse;
import com.faisal.rsas.response.BerkasResponse;
import com.faisal.rsas.response.LoginResponse;
import com.faisal.rsas.response.PasienResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> loginUser(
            @Field("username") String username,
            @Field("password") String password
    );

    @POST("api/pasien")
    Call<PasienResponse> getPasien(@Header("Authorization") String token);

    @GET("api/validasitoken")
    Call<ResponseBody> validateToken(@Header("Authorization") String token);

    @POST("api/list-berkas")
    Call<BerkasResponse> getBerkasList(
            @Header("Authorization") String authToken,
            @Body Map<String, String> noRawatBody
    );
    @Multipart
    @POST("api/tambah-berkas")
    Call<ResponseBody> tambahBerkas(
            @Header("Authorization") String authToken,
            @Part MultipartBody.Part file,
            @Part("no_rawat") RequestBody noRawat,
            @Part("kode") RequestBody kode
    );
    @Multipart
    @POST("api/upload-draft")
    Call<ResponseBody> uploadDraft(
            @Header("Authorization") String authToken,
            @Part MultipartBody.Part file,
            @Part("no_rawat") RequestBody noRawat
    );
    @FormUrlEncoded
    @POST("api/get-draft")
    Call<ResponseBody> getDraft(
            @Header("Authorization") String authToken,
            @Field("no_rawat") String noRawat
    );
    @Multipart
    @POST("api/tambah-berkas")
    Call<ResponseBody> uploadBerkas(
            @Header("Authorization") String authToken,
            @Part MultipartBody.Part file,
            @Part("no_rawat") RequestBody noRawat,
            @Part("kode") RequestBody kode
    );
    @GET("directions/json")
    Call<DirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String apiKey
    );
}
