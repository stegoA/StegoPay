package com.example.stegopaybeta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.stegopaybeta.usedclasses.Home;

public class CardDetails extends AppCompatActivity {

    public TextView returnHomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_details);

        //clicking the "return home" text on the bottom of the screen redirects the user back to the home page
        returnHomeText = (TextView) findViewById(R.id.returnHome);
        returnHomeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CardDetails.this, Home.class);
                startActivity(intent);
            }

        });
    }
}