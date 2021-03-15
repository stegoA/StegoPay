package com.example.stegopaybeta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.stegopaybeta.StegoPayUtils.BASE_URL;
import static com.example.stegopaybeta.StegoPayUtils.SHARED_PREF_NAME;
import static com.example.stegopaybeta.StegoPayUtils.getUnsafeOkHttpClient;
import static com.example.stegopaybeta.StegoPayUtils.getUserIDFromToken;

public class Home extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    // Views
    ListView lv_transactions;
    Button addCardButton, viewCardsButton;
    TextView userGreetingTextView, noTransactionsTextView;
    CircleImageView profileImageView;
    ProgressBar progressBar;
    ImageView noTransactionsImageView;


    Retrofit retrofit;

    TransactionAdapter transactionAdapter;
    ArrayList<Transaction> transactionList = new ArrayList<>();

    SharedPreferences sharedPreferences;
    DataBaseHelper dataBaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Initializing views
        lv_transactions = (ListView) findViewById(R.id.transactionsListView);
        addCardButton = (Button) findViewById(R.id.addCardButton);
        viewCardsButton = (Button) findViewById(R.id.viewCardsButton);
        userGreetingTextView = (TextView) findViewById(R.id.userGreetingTextView);
        profileImageView = (CircleImageView) findViewById(R.id.profileImageView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        noTransactionsImageView = (ImageView) findViewById(R.id.noTransactionsImageView);
        noTransactionsTextView = (TextView) findViewById(R.id.noTransactionsTextView);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build())
                .build();

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        dataBaseHelper = new DataBaseHelper(Home.this);



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


        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Profile.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);

        transactionList.clear();

        // Getting the JWT token from shared preferences
        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        // Getting the user's ID from the JWT token
        String userIdFromToken = getUserIDFromToken(tokenFromSharedPrefs);

        // Get user details from SQLite
        getUserDetails(userIdFromToken);


        //  dataBaseHelper.dropTable(userIdFromToken);
        getTransactionsFromServer(tokenFromSharedPrefs);
    }

    public String getTokenFromSharedPrefs() {
        String fromSharedPrefs = sharedPreferences.getString("Token", "");
        return fromSharedPrefs;
    }


    public void getUserDetails(String userID) {

        String firstName = dataBaseHelper.getUserFirstName(userID);
        String profileImage = dataBaseHelper.getUserProfileImage(userID);

        if (firstName != "N/A") {
            setUserGreetingTextView(firstName);
        }

        if (profileImage != "N/A") {
            setUserProfileImage(profileImage);
        }
    }


    public void setUserGreetingTextView(String userFirstName) {
        userGreetingTextView.setText("Hello, " + userFirstName + "!");
    }

    public void setUserProfileImage(String profileImage) {
            byte[] decode = Base64.decode(profileImage, Base64.DEFAULT);
            Bitmap decodedProfilePicture = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            profileImageView.setImageBitmap(decodedProfilePicture);
    }

    public void getTransactionsFromServer(String token) {

        StegoPayApi stegoPayApi = retrofit.create(StegoPayApi.class);

        Call<List<Transaction>> call = stegoPayApi.getAllTransactions( "Bearer " + token);

        call.enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if(!response.isSuccessful()) {
                    System.out.println("HTTP Code: " + response.code() + " " + response.message());
                    return;
                }

                List<Transaction> transactions = response.body();

                for (Transaction transaction: transactions) {
                    Transaction transactionObj = new Transaction(transaction.getVendor(), transaction.getAmount(), transaction.getCardID(), transaction.getDate(), transaction.getCurrency());
                    transactionList.add(transactionObj);
                }

                populateActivityListView();

            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println(t.getMessage());
            }
        });
    }


    public void populateActivityListView() {

        if (transactionList.isEmpty()) {
            noTransactionsImageView.setVisibility(View.VISIBLE);
            noTransactionsTextView.setVisibility(View.VISIBLE);
        } else {
            transactionAdapter = new TransactionAdapter(getApplicationContext(), transactionList);
            lv_transactions.setAdapter(transactionAdapter);
            noTransactionsImageView.setVisibility(View.GONE);
            noTransactionsTextView.setVisibility(View.GONE);
        }

        progressBar.setVisibility(View.GONE);

    }


    }

