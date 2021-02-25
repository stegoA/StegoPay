package com.example.stegopaybeta;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface StegoPayApi {

 @Headers({"Content-Type: application/json", "Accept: application/json"})
 @GET("getAllTransactions")
 Call<List<Transaction>> getAllTransactions (@Header("Authorization") String token);

 @POST("addCard")
 Call<Card> createCard(@Header("Authorization") String token, @Body Card card);



}
