package com.example.stegopaybeta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.cert.CertificateException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.stegopaybeta.StegoPayUtils.BASE_URL;
import static com.example.stegopaybeta.StegoPayUtils.getUnsafeOkHttpClient;
import static com.example.stegopaybeta.StegoPayUtils.isEmailValid;
import static com.example.stegopaybeta.StegoPayUtils.isPasswordValid;

public class SignUp extends AppCompatActivity
{
    private Retrofit retrofit;
    private StegoPayApi stegoPayApi;

    //Local database
    DataBaseHelper stegoPayDB;

    // Views
    public Button signUp;
    public TextView signUpBottomText;
    public EditText firstName;
    public EditText lastName;
    public EditText emailAddress;
    public EditText password;
    public EditText confirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Instance of retrofit class
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //converts JSON data received to a java object
                .client(getUnsafeOkHttpClient().build())
                .build();

        stegoPayApi = retrofit.create(StegoPayApi.class);

        //initialize db class
        stegoPayDB = new DataBaseHelper(this);

        firstName = (EditText) findViewById(R.id.firstNameEditText);
        lastName = (EditText) findViewById(R.id.lastNameEditText);
        emailAddress = (EditText) findViewById(R.id.emailAddressEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        confirmPassword = (EditText) findViewById(R.id.confirmPasswordEditText);
        signUpBottomText = (TextView) findViewById(R.id.logInRedirect);



        signUp = (Button) findViewById(R.id.signUpButton);
        signUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String getFirstName = firstName.getText().toString();
                String getLastName = lastName.getText().toString();
                String getEmailAddress = emailAddress.getText().toString();
                String getPassword = password.getText().toString();
                String getConfirmPassword = confirmPassword.getText().toString();

                //if any of the fields are left blank, alert user
                if (getFirstName.equals("") || getLastName.equals("") || getEmailAddress.equals("") || getPassword.equals("") || getConfirmPassword.equals("")) {
                    Toast.makeText(SignUp.this, "Fields cannot be left blank.", Toast.LENGTH_LONG).show();
                }

                //if user enters an invalid (wrong format) email
                else if (!isEmailValid(getEmailAddress)) {
                    emailAddress.setError("Please enter a valid email address.");
                } else if (!isPasswordValid(getPassword)) {
                    password.setError("Your password must be at least:\n" +
                            "\n" +
                            "8 characters long\n" +
                            "Include 1 uppercase & 1 lowercase character\n" +
                            "Include 1 number\n" +
                            "Include 1 special character");

                }

                //if the two passwords the user enters do not match
                else if (!getPassword.equals(getConfirmPassword)) {
                    password.setError("The passwords do not match.");
                } else {

                    signUpRequest();

                }

            }
        });


        signUpBottomText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //clicking the text at the bottom of the page redirects the user to the login screen
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
            }
        });

        //
//        stegoPayDB.dropCardsTable("603b78fb8319174c640710f9");
//        stegoPayDB.dropCardsTable("603e13e8cf2e2a08981a320b");
//        stegoPayDB.dropUserTable();

    }




    private void signUpRequest() {


        User user = new User(firstName.getText().toString(), lastName.getText().toString(), emailAddress.getText().toString(), password.getText().toString());

        Call<Void> call = stegoPayApi.callSignUp(user);

        //Make HTTP request
        call.enqueue(new Callback<Void>() {

            @Override
            //When HTTP request succeeds
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Toast.makeText(SignUp.this, "Signed up successfully.", Toast.LENGTH_LONG).show();

                    //redirect to log in activity so the now signed-up user can log in
                    Intent intent = new Intent(SignUp.this, LogIn.class);
                    startActivity(intent);

                } else if (response.code() == 500) {
                    Toast.makeText(SignUp.this, "An account with this email already exists. Please try again.", Toast.LENGTH_LONG).show();

                }

            }

            @Override
            //When HTTP request fails
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignUp.this, "An error has occurred. Please try again.", Toast.LENGTH_LONG).show();

            }
        });
    }




}

