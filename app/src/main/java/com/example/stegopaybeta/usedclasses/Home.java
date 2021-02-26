package com.example.stegopaybeta.usedclasses;

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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stegopaybeta.AddCardFinal;
import com.example.stegopaybeta.ListofCards;
import com.example.stegopaybeta.R;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Home extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private final String BASE_URL = "https://10.0.2.2:3443";

    public static final String SHARED_PREF_NAME = "MyPreferences";

    String userID;

    //Local database
    DBHelper stegoPayDB;

    //clickable elements
    public CircleImageView miniProfilePicture;
    public Button viewCardsButton;
    public Button addCardButton;

    //UI "Hello(User)"
    public TextView helloName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

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


        helloName = (TextView) findViewById(R.id.helloNameTextView);
        Cursor cursor = stegoPayDB.getUser(userID);
        cursor.moveToFirst();
        helloName.setText("Hello, " + cursor.getString(1) + "!");


        miniProfilePicture = (CircleImageView) findViewById(R.id.miniProfileImage);

        //convert base64 image retrieved from SQLite to bitmap to display on the UI
        String encodedProfilePicture = cursor.getString(4);
        if (encodedProfilePicture != null) {
            byte[] decodedProfilePicture = Base64.decode(encodedProfilePicture, Base64.DEFAULT);
            Bitmap decoded = BitmapFactory.decodeByteArray(decodedProfilePicture, 0, decodedProfilePicture.length);
            miniProfilePicture.setImageBitmap(decoded);
        }


        miniProfilePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Home.this, Profile.class);
                startActivity(intent);

            }

        });


        addCardButton = (Button) findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //clicking the "add a card" button redirects the user to a screen where they can add a card
                Intent intent = new Intent(Home.this, AddCardFinal.class);
                startActivity(intent);
            }

        });


        viewCardsButton = (Button) findViewById(R.id.viewCardsButton);
        viewCardsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //clicking the "view cards" button redirects the user to a screen with their list of cards
                Intent intent = new Intent(Home.this, ListofCards.class);
                startActivity(intent);
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

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
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



