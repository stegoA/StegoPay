package com.example.stegopaybeta.usedclasses;

import androidx.appcompat.app.AppCompatActivity;

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

import com.example.stegopaybeta.R;
import com.example.stegopaybeta.ResetPasswordScreen1;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
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

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LogIn extends AppCompatActivity {

    //Retrofit to connect to mongoDB database
    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private final String BASE_URL = "https://10.0.2.2:3443";

    //Shared preferences name
    public static final String SHARED_PREF_NAME = "MyPreferences";

    //Local database
    DBHelper stegoPayDB;

    //user's ID decoded from the JWT token
    public static String userID;

    //For preprocessing
    Steganography steganographyObj = new Steganography();
    boolean valid_image_selected;
    Bitmap coverImage;
    //pixel's location and its value in binary (retrieved from preprocessing image received from mongo)
    private HashMap<String, String> sampleHashMap_2 = new HashMap<>();


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

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        //instance of database class
        stegoPayDB = new DBHelper(this);


        //example hashmap2
        sampleHashMap_2.put("1X1", "010101010101011110101010111");

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
                Intent intent = new Intent(LogIn.this, ResetPasswordScreen1.class);
                startActivity(intent);
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

    //check if the email the user uses to sign up is valid
    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public static boolean isPasswordValid(String password) {
        String expression = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }


    public static String decodeJWTToken(String encodedJWT) throws Exception {
        //split the token string (which has 3 parts separated by a .)
        String[] split = encodedJWT.split("\\.");

        //Second part is the payload which includes the user id
        String payload = getJson(split[1]);

        //get ONLY user id from the whole payload
        JSONObject json = new JSONObject(payload);
        String userID = json.getString("_id");
        return userID;

    }

    //converts the decoded JWT info so it's readable
    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }


    private void logInRequest() {

        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        //Send values entered by the user to the mongoDB database
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("email", userEmailAddress.getText().toString());
        hmap.put("password", userPassword.getText().toString());

        Call<JWTToken> call = retrofitInterface.callLogIn(hmap);

        //Make HTTP request
        call.enqueue(new Callback<JWTToken>() {

            @Override
            //When HTTP request succeeds
            public void onResponse(Call<JWTToken> call, Response<JWTToken> response) {

                if (response.isSuccessful()) {

                    //token received when user logs in
                    JWTToken jwtToken = response.body();
                    String token = jwtToken.getJWTToken();


                    //save JWT token to shared preferences (so it can be accessed by other activities when called)
                    SharedPreferences myPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = myPreferences.edit();
                    editor.putString("token", token);
                    editor.commit();


                    //If log in is successful, add user to Users table (if they don't already exist) and assign them their own table user(id)
                    Call<UserObject> call2 = retrofitInterface.getProfile("Bearer " + token);


                    call2.enqueue(new Callback<UserObject>() {

                        @Override
                        public void onResponse(Call<UserObject> call, Response<UserObject> response) {

                            if (response.isSuccessful()) {

                                progressBar.setVisibility(View.GONE);

                                //decode JWT token
                                userID = null;
                                try {
                                    userID = decodeJWTToken(token);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                //Check if the logged-in already exists in the SQLite database based on their userId
                                Boolean checkUserID = stegoPayDB.checkUserId(userID);

                                //If they don't exist
                                if (checkUserID == false) {

                                    //Add user to the users table
                                    stegoPayDB.addUser(response.body().getUser().get_id(), response.body().getUser().getFirstName(), response.body().getUser().getLastName(),
                                            response.body().getUser().getEmail(), response.body().getUser().getProfileImage());

                                    //Create a blank table for the user where their added cards go
                                    stegoPayDB.create_Users_Cards_Table(response.body().getUser().get_id());


                                    //if user is logging in to this device for the first time
                                    Call<CardArray> call3 = retrofitInterface.getAllCards("Bearer " + token);
                                    call3.enqueue(new Callback<CardArray>() {

                                        @Override
                                        //When HTTP request succeeds
                                        public void onResponse(Call<CardArray> call, Response<CardArray> response) {

                                            if (response.isSuccessful()) {

                                                //get list of cards from the card array
                                                List<Card> cards = response.body().getCard();

                                                //loop through all cards received (if any)
                                                for (Card card : cards) {
                                                    String cardId = card.get_id();
                                                    String nickName = card.getNickName();
                                                    String image = card.getImage();
                                                    HashMap<Integer, String> hashMap1 = card.getHashMap_1();
                                                    String last4Digits = card.getLast4Digits();

                                                    //check if card already exists in the SQLite database
                                                    boolean checkCardId = stegoPayDB.checkCardId(userID, cardId);

                                                    //if it doesn't exist
                                                    if (checkCardId == false) {

                                                        //convert image received to bitmap
                                                        byte[] decode = Base64.decode(image, Base64.DEFAULT);
                                                        Bitmap decodedImage = BitmapFactory.decodeByteArray(decode, 0, decode.length);

                                                        //Preprocess the image received to get hashmap 2 (and 1 for now)
                                                        new preprocessing().execute(decodedImage);

                                                        //convert hashmap 1 received
                                                        String hashMap_1 = convertHashMapToString(hashMap1);

                                                        //sample hashmap2 for now (need new Steganography method to getHashMap2 to convert it)
                                                        String hashMap_2 = convertHashMapToString(sampleHashMap_2);

                                                        stegoPayDB.addCard(userID, cardId, nickName, image, hashMap_1, hashMap_2, last4Digits);
                                                    }
                                                }
                                            }
                                        }


                                        @Override
                                        public void onFailure(Call<CardArray> call, Throwable t) {

                                            progressBar.setVisibility(View.GONE);

                                            Toast.makeText(LogIn.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();

                                        }
                                    });
                                }

                                //if user is relogging in to this device after adding a card to their account in another device
                                Call<CardArray> call3 = retrofitInterface.getAllCards("Bearer " + token);
                                call3.enqueue(new Callback<CardArray>() {

                                    @Override
                                    //When HTTP request succeeds
                                    public void onResponse(Call<CardArray> call, Response<CardArray> response) {

                                        if (response.isSuccessful()) {

                                            //get list of cards from the card array
                                            List<Card> cards = response.body().getCard();

                                            //loop through all cards received (if any)
                                            for (Card card : cards) {
                                                String cardId = card.get_id();
                                                String nickName = card.getNickName();
                                                String image = card.getImage();
                                                HashMap<Integer, String> hashMap1 = card.getHashMap_1();
                                                String last4Digits = card.getLast4Digits();

                                                //check if card already exists in the SQLite database
                                                boolean checkCardId = stegoPayDB.checkCardId(userID, cardId);

                                                //if it doesn't exist
                                                if (checkCardId == false) {

                                                    //convert image received to bitmap
                                                    byte[] decode = Base64.decode(image, Base64.DEFAULT);
                                                    Bitmap decodedImage = BitmapFactory.decodeByteArray(decode, 0, decode.length);

                                                    //Preprocess the image received to get hashmap 2
                                                    new preprocessing().execute(decodedImage);

                                                    //convert hashmap 1 received
                                                    String hashMap_1 = convertHashMapToString(hashMap1);

                                                    //sample hashmap2 for now (need new Steganography method to getHashMap2 to convert it)
                                                    String hashMap_2 = convertHashMapToString(sampleHashMap_2);

                                                    stegoPayDB.addCard(userID, cardId, nickName, image, hashMap_1, hashMap_2, last4Digits);
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


                            } else if (response.code() == 401) {

                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(LogIn.this, "You are not authorized to access this section.", Toast.LENGTH_LONG).show();
                            }
                        }


                        @Override
                        public void onFailure(Call<UserObject> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(LogIn.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();

                        }


                    });
                } else if (response.code() == 402) {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(LogIn.this, "An account with the email " + hmap.get("email") + " does not exist.", Toast.LENGTH_LONG).show();

                } else {

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LogIn.this, "Invalid email/password combination.", Toast.LENGTH_LONG).show();

                }


            }

            @Override
            //When HTTP request fails
            public void onFailure(Call<JWTToken> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LogIn.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
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


    //Preprocessing AsyncTask
    private class preprocessing extends AsyncTask<Bitmap, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            //Set image for steganographyObj
            steganographyObj.setImage(bitmaps[0]);

            //Start Preprocessing
            steganographyObj.preProcessing2();

            //If the image selected is StegoPay ready
            if (steganographyObj.check_image_validity()) {
                //Set coverImage to the selected image
                coverImage = bitmaps[0];

                //Set valid image selected boolean to true
                valid_image_selected = true;

                //Return true
                return true;
            } else {
                //Set image to null
                steganographyObj.setImage(null);
                //Set valid image selected boolean to false
                valid_image_selected = false;
                //Return false
                return false;
            }
        }

    }

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}