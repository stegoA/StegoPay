package com.example.stegopaybeta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import static com.example.stegopaybeta.StegoPayUtils.JWT_TOKEN;
import static com.example.stegopaybeta.StegoPayUtils.SHARED_PREF_NAME;
import static com.example.stegopaybeta.StegoPayUtils.getUserIDFromToken;


public class ViewCards extends AppCompatActivity {


    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        setResult(RESULT_OK,i);
        super.onBackPressed();
    }

    // ListView to display the user's cards
    ListView lv_cards;
    ImageView noTransactionsImageView;
    TextView noTransactionsTextView;

    ArrayList<Card> allCards;

    CardAdapter cardAdapter;

    SharedPreferences sharedPreferences;

    DataBaseHelper db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cards);

        // Initializing ListView
        lv_cards = (ListView) findViewById(R.id.cardListView);
        noTransactionsImageView = (ImageView) findViewById(R.id.noTransactionsImageView);
        noTransactionsTextView = (TextView) findViewById(R.id.noTransactionsTextView);

        // Instantiating db
        db = new DataBaseHelper(this);


        lv_cards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), CardDetails.class);
                String cardID = allCards.get(position).getCardID();
                i.putExtra("cardId", cardID);
                startActivity(i);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Getting the JWT token from shared preferences
        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        // Getting the user ID from the JWT token
        String userIDFromToken = getUserIDFromToken(tokenFromSharedPrefs);

        // Getting user cards from SQLite
        getCardsFromSQLite(userIDFromToken);

        // Populating the cards ListView
        populateListView();
    }

    // Method to get the user's card(s) from SQLite
    public void getCardsFromSQLite(String userID) {
        System.out.println("VIEW CARDS: "+userID);
       allCards = db.getAllCards(userID);
    }

    // Method to populate the cards ListView
    public void populateListView() {

        cardAdapter = new CardAdapter(getApplicationContext(), allCards);
        lv_cards.setAdapter(cardAdapter);


        if (allCards.isEmpty()) {
            noTransactionsImageView.setVisibility(View.VISIBLE);
            noTransactionsTextView.setVisibility(View.VISIBLE);
        } else {
            noTransactionsImageView.setVisibility(View.GONE);
            noTransactionsTextView.setVisibility(View.GONE);
        }



    }

    // Method to get the user's JWT token from shared preferences
    public String getTokenFromSharedPrefs() {

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        String fromSharedPrefs = sharedPreferences.getString(JWT_TOKEN, "");

        return fromSharedPrefs;

    }

}
