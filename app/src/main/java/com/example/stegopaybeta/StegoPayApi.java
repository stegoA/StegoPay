package com.example.stegopaybeta;

import com.google.gson.JsonObject;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StegoPayApi {

 @POST("/signUp")
 Call<Void> callSignUp(@Body User user);

 @POST("/logInAndroid")
 Call<JWTToken> callLogIn(@Body User user);

 @GET("/profile")
 Call<JsonObject> getProfile(@Header("Authorization") String token);

 @GET("/getAllCards")
 Call<CardArray> getAllCards(@Header("Authorization") String token);

 @PATCH("/updateProfile")
 Call<User> editProfile(@Header("Authorization") String token, @Body HashMap<String, String> userHashMap);

 @Headers({"Content-Type: application/json", "Accept: application/json"})
 @GET("getAllTransactions")
 Call<List<Transaction>> getAllTransactions (@Header("Authorization") String token);

 @POST("addCard")
 Call<Card> createCard(@Header("Authorization") String token, @Body Card card);

 @GET("/getCard/{id}")
 Call<Card> getCard(@Path("id") String id, @Header("Authorization") String header);

 @GET("/getAllTransactionsOfACard/{cardId}")
 Call<ArrayList<Transaction>> getAllTransactionsOfACard(@Path("cardId") String cardId, @Header("Authorization") String header);

 @PATCH("/updateCard/{id}")
 Call<Card> updateCard(@Path("id") String id, @Header("Authorization") String header, @Body Card card);

 @DELETE("/deleteCard/{id}")
 Call<JsonObject> deleteCard(@Path("id") String id, @Header("Authorization") String header);



}
