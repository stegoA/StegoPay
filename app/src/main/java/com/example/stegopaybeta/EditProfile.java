package com.example.stegopaybeta;

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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.stegopaybeta.StegoPayUtils.BASE_URL;
import static com.example.stegopaybeta.StegoPayUtils.JWT_TOKEN;
import static com.example.stegopaybeta.StegoPayUtils.SHARED_PREF_NAME;
import static com.example.stegopaybeta.StegoPayUtils.getUnsafeOkHttpClient;
import static com.example.stegopaybeta.StegoPayUtils.getUserIDFromToken;
import static com.example.stegopaybeta.StegoPayUtils.isEmailValid;

public class EditProfile extends AppCompatActivity {

    private Retrofit retrofit;
    private StegoPayApi stegoPayApi;

    //Local database
    DataBaseHelper stegoPayDB;

    public CircleImageView profilePicture;
    public EditText firstName;
    public EditText lastName;
    public EditText emailAddress;
    public Button saveProfileChanges;
    public ProgressBar progressBar;


    SharedPreferences sharedPreferences;

    private static final int GALLERY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        profilePicture = (CircleImageView) findViewById(R.id.editProfileImage);
        firstName = (EditText) findViewById(R.id.editFirstName);
        lastName = (EditText) findViewById(R.id.editLastName);
        emailAddress = (EditText) findViewById(R.id.editEmailAddress);
        saveProfileChanges = (Button) findViewById(R.id.saveProfile);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //converts JSON data received to a java object
                .client(getUnsafeOkHttpClient().build())
                .build();

        stegoPayApi = retrofit.create(StegoPayApi.class);


        stegoPayDB = new DataBaseHelper(this);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        // Get JWT token from shared preferences
        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        // Decode JWT Token to get the user ID
        String userIDFromToken = getUserIDFromToken(tokenFromSharedPrefs);

        // Get the user's profile from SQLite
        Cursor cursor = getUserDetailsFromSQLite(userIDFromToken);

        // Set user profile details
        setUserProfileDetails(cursor);

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectProfilePicture();
            }
        });


        saveProfileChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                saveChanges();
            }
        });


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


            // To store chosen profile picture in SQLite

            //get JWT token from shared preferences (saved in LogIn class)
            SharedPreferences myPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
            String token = myPreferences.getString(JWT_TOKEN, "");

            //decode JWT token to get current logged-in user's ID
            String userIDFromToken = getUserIDFromToken(token);

            Cursor cursor = stegoPayDB.getUser(userIDFromToken);
            cursor.moveToFirst();
            stegoPayDB.updateUser(userIDFromToken, cursor.getString(1), cursor.getString(2), cursor.getString(3), encodedProfilePicture);

        }
    }


    public void editProfileRequest() {
        //get JWT token from shared preferences
        String tokenFromSharedPrefs = getTokenFromSharedPrefs();
        System.out.println("Token: " + tokenFromSharedPrefs);

        // Decode JWT Token to get the user ID
        String userIDFromToken = getUserIDFromToken(tokenFromSharedPrefs);

        // Get the user's profile from SQLite
        Cursor cursor = getUserDetailsFromSQLite(userIDFromToken);

        // Getting user details in a HashMap
        HashMap<String, String> userHashMap = getUser(cursor);

        //
        Call<User> call = stegoPayApi.editProfile("Bearer " + tokenFromSharedPrefs, userHashMap);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // Get updated user's profile
                    getUserProfileFromServer(tokenFromSharedPrefs);
                } else if (!response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditProfile.this, "You are not authorized to access this section", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfile.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void getUserProfileFromServer(String token) {
        Call<JsonObject> call2 =  stegoPayApi.getProfile("Bearer " + token);

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

                        // Update local db with new details
                        updateUserProfileTable(user);



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 401) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(EditProfile.this, "You are not authorized to access this section.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfile.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void updateUserProfileTable(User user) {
        stegoPayDB.updateUser(user.get_id(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getProfileImage());
        progressBar.setVisibility(View.GONE);
        Toast.makeText(EditProfile.this, "Profile saved!", Toast.LENGTH_LONG).show();
        finish();
        /*
        Intent intent = new Intent(EditProfile.this, Profile.class);
        startActivity(intent);*/
    }

    public HashMap<String, String> getUser(Cursor cursor) {
        cursor.moveToFirst();

        HashMap<String, String> userHashMap = new HashMap<>();
        userHashMap.put("firstName", firstName.getText().toString());
        userHashMap.put("lastName", lastName.getText().toString());
        userHashMap.put("email", emailAddress.getText().toString());
        userHashMap.put("profileImage", cursor.getString(4));

        return userHashMap;
    }



    public void saveChanges() {
        if (firstName.getText().toString().equals("") || lastName.getText().toString().equals("") || emailAddress.getText().toString().equals("")) {
            Toast.makeText(EditProfile.this, "Fields cannot be left blank.", Toast.LENGTH_LONG).show();
        } else if (!isEmailValid(emailAddress.getText().toString())) {
            Toast.makeText(EditProfile.this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
        } else {
            editProfileRequest();
        }
    }

    public void selectProfilePicture() {
        //Intent to go to gallery activity so user can choose a profile picture
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select your profile picture"), GALLERY_REQUEST_CODE);
    }

    public void setUserProfileDetails(Cursor cursor) {
        cursor.moveToFirst();

        firstName.setText(cursor.getString(1));
        lastName.setText(cursor.getString(2));
        emailAddress.setText(cursor.getString(3));

        String encodedProfilePicture = cursor.getString(4);

        if (encodedProfilePicture != null) {
            byte[] decode = Base64.decode(encodedProfilePicture, Base64.DEFAULT);
            Bitmap decodedProfilePicture = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            profilePicture.setImageBitmap(decodedProfilePicture);
        }
    }

    public Cursor getUserDetailsFromSQLite(String userID) {
        Cursor cursor = stegoPayDB.getUser(userID);
        return cursor;
    }



    public String getTokenFromSharedPrefs() {
        String fromSharedPrefs = sharedPreferences.getString(JWT_TOKEN, "");
        return fromSharedPrefs;
    }


}
