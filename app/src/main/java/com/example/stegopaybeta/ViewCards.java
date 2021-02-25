package com.example.stegopaybeta;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import static com.example.stegopaybeta.Home.JWT_TOKEN;
import static com.example.stegopaybeta.Home.SHARED_PREFS;
import static com.example.stegopaybeta.Home.getUserIDFromToken;

public class ViewCards extends AppCompatActivity {

    ListView lv_cards;

    CardAdapter cardAdapter;

    SharedPreferences sharedPreferences;


    DataBaseHelper db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cards);

        lv_cards = (ListView) findViewById(R.id.cardListView);

        db = new DataBaseHelper(this);

        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        String userIDFromToken = getUserIDFromToken(tokenFromSharedPrefs);

        populateListView(userIDFromToken);

    }


    public void populateListView(String userID) {

        ArrayList<Card> allCards = db.getAllCards(userID);

        cardAdapter = new CardAdapter(this, allCards);

        lv_cards.setAdapter(cardAdapter);

    }


    public String getTokenFromSharedPrefs() {

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        String fromSharedPrefs = sharedPreferences.getString(JWT_TOKEN, "");

       // System.out.println("The token I got from sharedPrefs is: " + fromSharedPrefs);

        return fromSharedPrefs;

    }

}
