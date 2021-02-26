package com.example.stegopaybeta.usedclasses;

import com.example.stegopaybeta.usedclasses.CardArray;
import com.example.stegopaybeta.usedclasses.JWTToken;
import com.example.stegopaybeta.usedclasses.UserObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface RetrofitInterface {


    @POST("/signUp")
    Call<Void> callSignUp(@Body HashMap<String, String> hmap);


    @POST("/logInAndroid")
    Call<JWTToken> callLogIn(@Body HashMap<String, String> hmap);


    @GET("/profile")
    Call<UserObject> getProfile(@Header("Authorization") String token);


    @PATCH("/updateProfile")
    Call<UserObject> editProfile(@Header("Authorization") String token,
                                 @Body HashMap<String, String> hmap);

    @GET("/getAllCards")
    Call<CardArray> getAllCards(@Header("Authorization") String token);


}



