package com.example.stegopaybeta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Home extends AppCompatActivity {

    ListView lv_transactions;
    Button addCardButton, viewCardsButton;
    TextView userGreetingTextView;

    TransactionAdapter transactionAdapter;
    ArrayList<Transaction> transactionList = new ArrayList<>();

    SharedPreferences sharedPreferences;
    DataBaseHelper dataBaseHelper;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String JWT_TOKEN = "JWT_TOKEN";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);


        lv_transactions = (ListView) findViewById(R.id.transactionsListView);
        addCardButton = (Button) findViewById(R.id.addCardButton);
        viewCardsButton = (Button) findViewById(R.id.viewCardsButton);
        userGreetingTextView = (TextView) findViewById(R.id.userGreetingTextView);

        
        // populateActivityListView

        // Initializing shared prefs
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Sample JWT Token 
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MDJmYTk2OTAwNjE5ZjU2MzhmMDVlZTciLCJpYXQiOjE2MTM3MzY2NjR9.eZBq_r2T2ek5kI3zc_jIudoIoGCxMP2PNOgZcpzDAqM";

        // Inserting JWT token into shared prefs
        editor.putString(JWT_TOKEN, token);
        editor.apply();


        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        String userIdFromToken = getUserIDFromToken(tokenFromSharedPrefs);

        setUserGreetingTextView(userIdFromToken);

       //  dataBaseHelper.dropTable(userIdFromToken);



//        UserProfileModel userProfileModel = new UserProfileModel(userIdFromToken, "Albert", "Einstein", "nuclear@gmail.com");
//        boolean success = dataBaseHelper.addUserProfile(userProfileModel);
//        System.out.println("Success = " + success);
        

        populateActivityListView(token);


        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AddCard.class);
                startActivity(i);
            }
        });



        viewCardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i = new Intent(getApplicationContext(), ViewCards.class);
               startActivity(i);
            }
        });




    }




    public String getTokenFromSharedPrefs() {

        String fromSharedPrefs = sharedPreferences.getString(JWT_TOKEN, "");

    //    System.out.println("The token I got from sharedPrefs is: " + fromSharedPrefs);

        return fromSharedPrefs;

    }


    public static String getUserIDFromToken(String token) {
        JWT jwt = new JWT(token);
        Claim claim = jwt.getClaim("_id");
        String userIdFromJWTToken = claim.asString();

       // System.out.println("The user id i got from the JWT token is: " + claim.asString());

        return userIdFromJWTToken;
    }


    public void setUserGreetingTextView(String userIdFromToken) {

        dataBaseHelper = new DataBaseHelper(Home.this);

        UserProfileModel userProfile = dataBaseHelper.getUserProfile(userIdFromToken);

        if (userProfile.getUserID().equals(userIdFromToken)) {
            System.out.println("Found: " + userProfile.getUserID());
            userGreetingTextView.setText("Hello, " + userProfile.getFirstName() + "!");
            dataBaseHelper.create_Users_Cards_Table(userIdFromToken);
        } else {
            Toast.makeText(getApplicationContext(), "Error retrieving profile.", Toast.LENGTH_SHORT).show();
            // Send to login screen
        }


    }


    public void populateActivityListView(String jwt) {

        String finalJWT = "Bearer " + jwt;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://10.0.2.2:3443/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build())
                .build();

        StegoPayApi stegoPayApi = retrofit.create(StegoPayApi.class);

        Call<List<Transaction>> call = stegoPayApi.getAllTransactions(finalJWT);

        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if(!response.isSuccessful()) {
                    System.out.println("HTTP Code: " + response.code() + " " + response.message());
                    return;
                }

                List<Transaction> transactions = response.body();

                for (Transaction transaction: transactions) {
                    Transaction transactionObj = new Transaction(transaction.getVendor(), transaction.getAmount(), transaction.getCardID());
                    transactionList.add(transactionObj);
                }

                transactionAdapter = new TransactionAdapter(getApplicationContext(), transactionList);
                lv_transactions.setAdapter(transactionAdapter);

            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
            Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println(t.getMessage());
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
