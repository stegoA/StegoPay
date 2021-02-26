package com.example.stegopaybeta.usedclasses;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stegopaybeta.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfile extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private final String BASE_URL = "https://10.0.2.2:3443";

    public static final String SHARED_PREF_NAME = "MyPreferences";

    String userID;

    //Local database
    DBHelper stegoPayDB;

    //gallery request code
    private static final int GALLERY_REQUEST_CODE = 1;

    //editable elements
    public CircleImageView profilePicture;
    public EditText firstName;
    public EditText lastName;
    public EditText emailAddress;

    //button to save profile changes
    public Button saveProfileChanges;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        //instance of retrofit class
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //converts JSON data received to a java object
                .client(getUnsafeOkHttpClient().build())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        //instance of database class
        stegoPayDB = new DBHelper(this);

        //get JWT token from shared preferences (saved in LogIn class)
        SharedPreferences myPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String token = myPreferences.getString("token", "");

        //decode JWT token to get current logged-in user's ID
        userID = null;
        try {
            userID = decodeJWTToken(token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cursor cursor = stegoPayDB.getUser(userID);
        cursor.moveToFirst();

        //display user details retrieved from SQLite
        firstName = (EditText) findViewById(R.id.editFirstName);
        firstName.setText(cursor.getString(1));

        lastName = (EditText) findViewById(R.id.editLastName);
        lastName.setText(cursor.getString(2));

        emailAddress = (EditText) findViewById(R.id.editEmailAddress);
        emailAddress.setText(cursor.getString(3));

        profilePicture = (CircleImageView) findViewById(R.id.editProfileImage);

        //convert currently-base64 image to bitmap to display on the UI
        String encodedProfilePicture = cursor.getString(4);
        if (encodedProfilePicture != null) {
            byte[] decodedProfilePicture = Base64.decode(encodedProfilePicture, Base64.DEFAULT);
            Bitmap decoded = BitmapFactory.decodeByteArray(decodedProfilePicture, 0, decodedProfilePicture.length);
            profilePicture.setImageBitmap(decoded);
        }

        profilePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Intent to go to gallery activity so user can choose a profile picture
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select your profile picture"), GALLERY_REQUEST_CODE);
            }

        });


        saveProfileChanges = (Button) findViewById(R.id.saveProfile);
        saveProfileChanges.setOnClickListener(new View.OnClickListener() {

            //on click of save profile changes, make request to update changes
            @Override
            public void onClick(View view) {

                if (firstName.getText().toString().equals("") || lastName.getText().toString().equals("") || emailAddress.getText().toString().equals("")) {
                    Toast.makeText(EditProfile.this, "Fields cannot be left blank.", Toast.LENGTH_LONG).show();
                } else if (!isEmailValid(emailAddress.getText().toString())) {
                    Toast.makeText(EditProfile.this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
                } else {
                    editProfileRequest();
                }

            }


        });
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

    //to check if new email the user enters is valid
    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    //Method to retrieve image URI, and also to convert the image to a bitmap (so that it can be base64 encoded and stored in mongoDB and SQLite)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            //retrieve image from URI
            Uri imageData = data.getData();

            //to store image selected as a bitmap
            Bitmap temp = null;
            try {
                //Convert image to bitmap
                InputStream imageStream = getContentResolver().openInputStream(imageData);
                temp = BitmapFactory.decodeStream(imageStream);
                profilePicture.setImageBitmap(temp);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Convert image bitmap to base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            temp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedProfilePicture = Base64.encodeToString(byteArray, Base64.DEFAULT);


            //v To store chosen profile picture in SQLite

            //get JWT token from shared preferences (saved in LogIn class)
            SharedPreferences myPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            String token = myPreferences.getString("token", "");

            //decode JWT token to get current logged-in user's ID
            userID = null;
            try {
                userID = decodeJWTToken(token);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Cursor cursor = stegoPayDB.getUser(userID);
            cursor.moveToFirst();
            stegoPayDB.updateUser(userID, cursor.getString(1), cursor.getString(2), cursor.getString(3), encodedProfilePicture);

        }


    }

    public void editProfileRequest() {

        //get JWT token from shared preferences (saved in LogIn class)
        SharedPreferences myPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String token = myPreferences.getString("token", "");

        //get base64 encoded image which has been stored in SQLite to pass to the hashmap to store in the mongodb database
        myPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userPFP = myPreferences.getString("userPFP", "");

        //decode JWT token to get current logged-in user's ID
        userID = null;
        try {
            userID = decodeJWTToken(token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cursor cursor = stegoPayDB.getUser(userID);
        cursor.moveToFirst();

        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("profileImage", cursor.getString(4));
        hmap.put("firstName", firstName.getText().toString());
        hmap.put("lastName", lastName.getText().toString());
        hmap.put("email", emailAddress.getText().toString());


        Call<UserObject> call = retrofitInterface.editProfile("Bearer " + token, hmap);

        call.enqueue(new Callback<UserObject>() {
            @Override
            public void onResponse(Call<UserObject> call, Response<UserObject> response) {

                if (response.isSuccessful()) {


                    //to get new profile details from mongoDB and use them to update the SQLite users table
                    Call<UserObject> call2 = retrofitInterface.getProfile("Bearer " + token);


                    call2.enqueue(new Callback<UserObject>() {

                        @Override
                        public void onResponse(Call<UserObject> call, Response<UserObject> response) {

                            if (response.isSuccessful()) {

                                //update users table with any new details
                                stegoPayDB.updateUser(response.body().getUser().get_id(), response.body().getUser().getFirstName(), response.body().getUser().getLastName(),
                                        response.body().getUser().getEmail(), response.body().getUser().getProfileImage());

                                Toast.makeText(EditProfile.this, "Profile saved!", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(EditProfile.this, Profile.class);
                                startActivity(intent);


                            } else if (response.code() == 401) {

                                Toast.makeText(EditProfile.this, "You are not authorized to access this section.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserObject> call, Throwable t) {
                            Toast.makeText(EditProfile.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();

                        }
                    });


                } else if (!response.isSuccessful()) {
                    Toast.makeText(EditProfile.this, "You are not authorized to access this section", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<UserObject> call, Throwable t) {

                Toast.makeText(EditProfile.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();

            }

        });

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







