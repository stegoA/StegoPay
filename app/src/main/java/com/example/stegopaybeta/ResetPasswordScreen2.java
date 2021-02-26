package com.example.stegopaybeta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.stegopaybeta.usedclasses.LogIn;

public class ResetPasswordScreen2 extends AppCompatActivity {

    public Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_2);

        //clicking the "reset password" button redirects the user back to the login screen
        resetPasswordButton = (Button) findViewById(R.id.resetPassword);
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResetPasswordScreen2.this, LogIn.class);
                startActivity(intent);
            }

        });
    }
}