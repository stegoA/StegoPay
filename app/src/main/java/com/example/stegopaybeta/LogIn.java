package com.example.stegopaybeta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.stegopaybeta.StegoPayUtils.BASE_URL;
import static com.example.stegopaybeta.StegoPayUtils.SHARED_PREF_NAME;
import static com.example.stegopaybeta.StegoPayUtils.getUnsafeOkHttpClient;
import static com.example.stegopaybeta.StegoPayUtils.isEmailValid;
import static com.example.stegopaybeta.StegoPayUtils.isPasswordValid;

public class LogIn extends AppCompatActivity {
    //Retrofit to connect to mongoDB database
    private Retrofit retrofit;
    private StegoPayApi stegoPayApi;


    //Local database
    DataBaseHelper stegoPayDB;


    //Clickable elements
    public Button logInButton;
    public TextView forgotPasswordText;
    public TextView logInBottomText;

    //User entries
    public EditText userEmailAddress;
    public EditText userPassword;

    //spinning progress bar at onClick of Login
    public ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);


        //instance of retrofit class
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //converts JSON data received to a java object
                .client(getUnsafeOkHttpClient().build())
                .build();

        stegoPayApi = retrofit.create(StegoPayApi.class);

        //instance of database class
        stegoPayDB = new DataBaseHelper(this);



        userEmailAddress = (EditText) findViewById(R.id.userEmailEditText);
        userPassword = (EditText) findViewById(R.id.userPasswordEditText);

        logInButton = (Button) findViewById(R.id.logIn);

        logInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String getUserEmailAddress = userEmailAddress.getText().toString();
                String getUserPassword = userPassword.getText().toString();

                //if any of the fields are left blank, alert user
                if (getUserEmailAddress.equals("") || getUserPassword.equals("")) {
                    Toast.makeText(LogIn.this, "Fields cannot be left blank.", Toast.LENGTH_SHORT).show();

                }

                //if user enters an invalid (wrong format) email
                else if (!isEmailValid(getUserEmailAddress)) {
                    userEmailAddress.setError("Please enter a valid email address.");
                }

                //if user tries to log in with a password that does not meet the initial requirements, they are notified of this
                else if (!isPasswordValid(getUserPassword)) {
                    userPassword.setError("Invalid password");
                } else {
                    logInRequest();

                }
            }
        });

        forgotPasswordText = (TextView) findViewById(R.id.resetPasswordRedirect);
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //clicking the "forgot password?" text redirects the user to the reset password page
                //Intent intent = new Intent(LogIn.this, ResetPasswordScreen1.class);
               // startActivity(intent);
            }
        });


        logInBottomText = (TextView) findViewById(R.id.signUpRedirect);
        logInBottomText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //clicking the text at the bottom of the page redirects the user to the sign up screen
                Intent intent = new Intent(LogIn.this, SignUp.class);
                startActivity(intent);
            }
        });



    }

    public void storeTokenInSharedPrefs(String jwtToken) {
        SharedPreferences myPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPreferences.edit();
        editor.putString("Token", jwtToken);
        editor.apply();
    }


    private void logInRequest() {
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        User user = new User(userEmailAddress.getText().toString(), userPassword.getText().toString());

        Call<JWTToken> call = stegoPayApi.callLogIn(user);

        //Make HTTP request
        call.enqueue(new Callback<JWTToken>() {
            @Override
            public void onResponse(Call<JWTToken> call, Response<JWTToken> response) {
                if (response.isSuccessful()) {
                    // Token received from the body of the response
                    String jwtToken = response.body().toString();

                    // Store token in shared preferences
                    storeTokenInSharedPrefs(jwtToken);

                    // Get user's profile from the server
                    getProfileFromServer(jwtToken);

                } else if (response.code() == 402) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LogIn.this, "An account with the email " + user.getEmail() + " does not exist.", Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LogIn.this, "Invalid email/password combination.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JWTToken> call, Throwable t) {
                System.out.println(t.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LogIn.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

    }


    public void getProfileFromServer(String jwtToken) {

        Call<JsonObject> call2 = stegoPayApi.getProfile("Bearer " + jwtToken);

        call2.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {

                    try {
                        // Parsing JSON response
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body().get("user")));

                        // Storing response parameters
                        String userID = jsonObject.getString("_id");
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");
                        String email = jsonObject.getString("email");
                        String profileImage = jsonObject.getString("profileImage");

                        User user = new User(userID, firstName, lastName, email, profileImage);

                        // Get user's cards from the server
                        getUserCardsFromServer(jwtToken, user);
                        


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                System.out.println("Error: " + t.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LogIn.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getUserCardsFromServer(String jwtToken, User user)  {

        Call<CardArray> call3 = stegoPayApi.getAllCards("Bearer " + jwtToken);

        call3.enqueue(new Callback<CardArray>() {
            @Override
            public void onResponse(Call<CardArray> call, Response<CardArray> response) {
                if (response.isSuccessful()) {

                    progressBar.setVisibility(View.GONE);

                    // Getting user's card details from the response
                    ArrayList<Card> cardsList = response.body().getCard();

                    // Checking if the user exists in the SQLite DB
                        boolean userExists = checkIfUserExistsInSQLite(user.get_id());

                        // If userID does not exist, add it to the user's table and create a table to store the user's cards
                        if (!userExists) {
                            // Add user to users table
                            addUserToUserTable(user.get_id(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getProfileImage());
                            // Create user_userId table to store the user's cards
                            createUsersCardTable(user.get_id());

                            for (Card card : cardsList) {

                                boolean cardExists = checkIfCardExistsInSQLite(user.get_id(), card.getCardID());

                                if (!cardExists) {
                                    // Add card to user's card table in the local db
                                    addCardToCardsTable(card.getCardID(), card.getNickName(), card.getLast4Digits(), user.get_id());
                                }

                            }


                        } else {

                            for (Card card : cardsList) {

                                boolean cardExists = checkIfCardExistsInSQLite(user.get_id(), card.getCardID());
                                System.out.println("Card exists: " + cardExists);

                                if (!cardExists) {
                                    // Add card to user's card table in the local db
                                    System.out.println("I am inside !cardExists");


                                    addCardToCardsTable(card.getCardID(), card.getNickName(), card.getLast4Digits(), user.get_id());
                                }

                            }

                        }
                    Intent intent = new Intent(LogIn.this, Home.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<CardArray> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LogIn.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void addCardToCardsTable(String cardID, String nickName, String last4Digits, String userID) {
        stegoPayDB.addCard(userID, cardID, nickName, last4Digits);
    }

    public boolean checkIfCardExistsInSQLite(String userID, String cardID) {
        boolean cardExists = stegoPayDB.checkCardId(userID, cardID);
        return cardExists;
    }

    // A method that checks if the userID exists in the SQLite DB, it returns a boolean
    public boolean checkIfUserExistsInSQLite(String userID) {
        Boolean userExists = stegoPayDB.checkUserId(userID);
        return userExists;
    }

    // A method that adds a user to the users table
    public void addUserToUserTable(String userID, String firstName, String lastName, String email, String profileImage) {
        stegoPayDB.addUser(userID, firstName, lastName, email, profileImage);
    }

    // A method that creates the table that stores the user's cards
    public void createUsersCardTable(String userID) {
        stegoPayDB.create_Users_Cards_Table(userID);
    }

    //Converting a hashmap to string
    public String convertHashMapToString(HashMap toConvert) {
        String converted = "";
        Iterator hmIterator = toConvert.entrySet().iterator();

        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) hmIterator.next();
            converted += mapElement.getKey() + ":" + mapElement.getValue() + ";";
        }
        return converted;
    }



    }


