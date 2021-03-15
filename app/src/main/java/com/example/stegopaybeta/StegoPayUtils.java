package com.example.stegopaybeta;

import android.content.SharedPreferences;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

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

public final class StegoPayUtils {

 public static final String BASE_URL = "https://10.0.2.2:3443";
 public static final String SHARED_PREF_NAME = "MyPreferences";
 public static final String JWT_TOKEN = "Token";

    public static String getUserIDFromToken(String token) {
        JWT jwt = new JWT(token);
        Claim claim = jwt.getClaim("_id");
        String userIdFromJWTToken = claim.asString();

        return userIdFromJWTToken;
    }



    //check if password the user enters is valid
    //Requirements: At least eight characters, with at least one uppercase letter, one lowercase letter, one number and one special character
    public static boolean isPasswordValid(String password) {
        String expression = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    //check if the email the user uses to sign up is valid
    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
