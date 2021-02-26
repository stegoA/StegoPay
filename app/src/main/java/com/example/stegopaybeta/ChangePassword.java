package com.example.stegopaybeta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.stegopaybeta.usedclasses.Profile;

public class ChangePassword extends AppCompatActivity {

    public Button confirmPasswordChangeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        //clicking the "change password" button on the change password screen redirects the user back to their profile
        confirmPasswordChangeButton = (Button) findViewById(R.id.confirmChangePassword);
        confirmPasswordChangeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangePassword.this, Profile.class);
                startActivity(intent);
            }

        });
    }
}