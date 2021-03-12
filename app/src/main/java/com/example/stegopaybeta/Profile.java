package com.example.stegopaybeta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;


import static com.example.stegopaybeta.StegoPayUtils.JWT_TOKEN;
import static com.example.stegopaybeta.StegoPayUtils.SHARED_PREF_NAME;
import static com.example.stegopaybeta.StegoPayUtils.getUserIDFromToken;

public class Profile extends AppCompatActivity {

    //Local database
    DataBaseHelper stegoPayDB;

    public ImageButton editProfile;
    public CircleImageView profilePicture;
    public TextView firstName;
    public TextView lastName;
    public TextView emailAddress;
    public Button changePasswordButton;
    public TextView returnHomeText;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        editProfile = (ImageButton) findViewById(R.id.editProfileImgButton);
        profilePicture = (CircleImageView) findViewById(R.id.profileImage);
        firstName = (TextView) findViewById(R.id.firstNameTextView);
        lastName = (TextView) findViewById(R.id.lastNameTextView);
        emailAddress = (TextView) findViewById(R.id.emailAddressTextView);
        changePasswordButton = (Button) findViewById(R.id.changePasswordButton);


        //instance of database class
        stegoPayDB = new DataBaseHelper(this);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        /* IN ON RESUME
        // Get JWT token from shared preferences
        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        // Decode JWT Token to get the user ID
        String userIDFromToken = getUserIDFromToken(tokenFromSharedPrefs);

        // Get the user's profile from SQLite
        Cursor cursor = getUserFromSQLite(userIDFromToken);

        // Set user profile details
        setUserProfileDetails(cursor);*/

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clicking the edit profile icon on the top right redirects the user to a screen where they can change their details
                Intent intent = new Intent(Profile.this, EditProfile.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get JWT token from shared preferences
        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        // Decode JWT Token to get the user ID
        String userIDFromToken = getUserIDFromToken(tokenFromSharedPrefs);

        // Get the user's profile from SQLite
        Cursor cursor = getUserFromSQLite(userIDFromToken);

        // Set user profile details
        setUserProfileDetails(cursor);
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

    public Cursor getUserFromSQLite(String userID) {
        Cursor cursor = stegoPayDB.getUser(userID);
        return cursor;
    }

    public String getTokenFromSharedPrefs() {
        String fromSharedPrefs = sharedPreferences.getString(JWT_TOKEN, "");
        return fromSharedPrefs;
    }

}
