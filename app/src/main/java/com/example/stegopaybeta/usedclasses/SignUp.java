package com.example.stegopaybeta.usedclasses;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stegopaybeta.R;

import java.security.cert.CertificateException;
import java.util.HashMap;
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

public class SignUp extends AppCompatActivity {


    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "https://10.0.2.2:3443";

    //Local database
    DBHelper stegoPayDB;

    //clickable elements
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


        //instance of retrofit class
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //converts JSON data received to a java object
                .client(getUnsafeOkHttpClient().build())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        //initialize db class
        stegoPayDB = new DBHelper(this);


        firstName = (EditText) findViewById(R.id.firstNameEditText);
        lastName = (EditText) findViewById(R.id.lastNameEditText);
        emailAddress = (EditText) findViewById(R.id.emailAddressEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        confirmPassword = (EditText) findViewById(R.id.confirmPasswordEditText);


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


        signUpBottomText = (TextView) findViewById(R.id.logInRedirect);
        signUpBottomText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //clicking the text at the bottom of the page redirects the user to the login screen
                Intent intent = new Intent(SignUp.this, LogIn.class);
                startActivity(intent);
            }
        });


    }


    //check if the email the user uses to sign up is valid
    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    //check if password the user enters is valid
    //Requirements: At least eight characters, with at least one uppercase letter, one lowercase letter, one number and one special character
    public static boolean isPasswordValid(String password) {
        String expression = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();

    }

    private void signUpRequest() {

        HashMap<String, String> hmap = new HashMap<>();

        //Get values entered by the user
        hmap.put("firstName", firstName.getText().toString());
        hmap.put("lastName", lastName.getText().toString());
        hmap.put("email", emailAddress.getText().toString());
        hmap.put("password", password.getText().toString());

        Call<Void> call = retrofitInterface.callSignUp(hmap);

        //Make HTTP request
        call.enqueue(new Callback<Void>() {

            @Override
            //When HTTP request succeeds
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 200) {
                    Toast.makeText(SignUp.this, "Signed up successfully. Welcome to StegoPay!", Toast.LENGTH_LONG).show();

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


    





