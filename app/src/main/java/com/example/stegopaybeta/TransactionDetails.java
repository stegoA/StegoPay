package com.example.stegopaybeta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.stegopaybeta.StegoPayUtils.BASE_URL;
import static com.example.stegopaybeta.StegoPayUtils.SHARED_PREF_NAME;
import static com.example.stegopaybeta.StegoPayUtils.getUnsafeOkHttpClient;

public class TransactionDetails extends AppCompatActivity {

    // Views
    TextView vendorTextView, totalPurchasesTextView, totalSpendingTextView, avgSpendingTextView, yourStatsTextView, transactionDateTextView, transactionCurrencyTextView;
    ListView itemListView;

    ItemAdapter itemAdapter;
    ArrayList<Item> itemList = new ArrayList<>();

    // To store vendor name passed from CardDetails class
    String vendorName;

    //Initializing retrofit interface
    Retrofit retrofit;
    StegoPayApi stegoPayApi;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_details);

        // Initializing views
        itemListView = (ListView) findViewById(R.id.itemListView);
        vendorTextView = (TextView) findViewById(R.id.vendorTextView);
        totalPurchasesTextView = (TextView) findViewById(R.id.totalPurchasesValue);
        totalSpendingTextView = (TextView) findViewById(R.id.totalSpendingValue);
        avgSpendingTextView = (TextView) findViewById(R.id.avgSpendingValue);
        yourStatsTextView = (TextView) findViewById(R.id.yourStatsTextView);
        transactionDateTextView = (TextView) findViewById(R.id.transactionDateTextView);
        transactionCurrencyTextView = (TextView) findViewById(R.id.currencyValueTextView);

        // Initializing retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build())
                .build();

        stegoPayApi = retrofit.create(StegoPayApi.class);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        // Getting the ArrayList of items, vendor name, and transactionDate from CardDetails
        Intent intent = getIntent();
        itemList = intent.getParcelableArrayListExtra("Transaction");
        vendorName = intent.getStringExtra("Vendor Name");
        String transactionDate = intent.getStringExtra("Transaction Date");
        String transactionCurrency = intent.getStringExtra("Currency");

        // A method to set the transaction date TextView
        setTransactionDate(transactionDate);

        // A method to set the currency TextView
        setTransactionCurrency(transactionCurrency);

        // Getting the JWT token from shared preferences
        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        // Get the user's stats
        getUserStats(tokenFromSharedPrefs);

        // Populate itemsListView with items purchased
        populateItemsListView();



    }

    // A method that takes a transaction currency string and sets the TextView
    private void setTransactionCurrency(String transactionCurrency) {
        transactionCurrencyTextView.setText(transactionCurrency);
    }

    // A method that takes a transaction date string, formats it, and sets the TextView
    private void setTransactionDate(String transactionDate) {
        SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputDate = new SimpleDateFormat("MMM. dd, yyyy");

        try {
            Date currentDate = inputDate.parse(transactionDate.split("T")[0]);
            transactionDateTextView.setText(outputDate.format(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // A method that makes a call to the backend to get the user's stats with a specific vender
    private void getUserStats(String token) {

        //Call to get all transactions made by this card
        Call<UserStats> call = stegoPayApi.getUserStats(vendorName,  "Bearer " + token);

        call.enqueue(new Callback<UserStats>() {
            @Override
            public void onResponse(Call<UserStats> call, Response<UserStats> response) {

                if (!response.isSuccessful()) {
                    System.out.println(response.code());
                    Toast.makeText(getApplicationContext(), "Unable to connect to the server, Please Try Again Later", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                UserStats userStats = new UserStats(response.body().get_id(), response.body().getVendorName(), response.body().getTotalSpending(), response.body().getTotalPurchases());

                // Set stats
                setViews(userStats);

            }

            @Override
            public void onFailure(Call<UserStats> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to connect to the server, Please Try Again Later", Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }


    // A method that takes the user's stats as input and sets the TextViews accordingly
    private void setViews(UserStats userStats) {

    yourStatsTextView.setText("Your stats with " + userStats.getVendorName() + ": ");
    vendorTextView.setText(userStats.getVendorName());
    totalPurchasesTextView.setText(userStats.getTotalPurchases());
    totalSpendingTextView.setText(userStats.getTotalSpending());

    Double avgSpending = Double.parseDouble(userStats.getTotalSpending()) / Integer.parseInt(userStats.getTotalPurchases());
    avgSpendingTextView.setText(String.format("%.2f", Double.parseDouble(avgSpending.toString())));
    }

    // A method that populates the itemsListView with items that the user purchased
    private void populateItemsListView() {
        itemAdapter = new ItemAdapter(getApplicationContext(), itemList);
        itemListView.setAdapter(itemAdapter);
    }

    // A method that gets the JWT token stored in SharedPreferences
    public String getTokenFromSharedPrefs() {
        String fromSharedPrefs = sharedPreferences.getString("Token", "");
        return fromSharedPrefs;
    }
}
