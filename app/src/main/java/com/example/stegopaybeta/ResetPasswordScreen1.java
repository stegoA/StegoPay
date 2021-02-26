package com.example.stegopaybeta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ResetPasswordScreen1 extends AppCompatActivity {

    public Button requestPasswordResetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_1);

        //clicking the "request password reset" button redirects the user to the next reset password screen
        requestPasswordResetButton = (Button) findViewById(R.id.requestPasswordReset);
        requestPasswordResetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResetPasswordScreen1.this, ResetPasswordScreen2.class);
                startActivity(intent);
            }

        });
    }
}