package com.example.stegopaybeta;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity  {

    EditText plaintextEditText;
    TextView plaintextTextView, ciphertextTextView;

    Encryption encryption;
    Decryption decryption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        encryption = new Encryption();
        decryption = new Decryption();


        plaintextEditText = (EditText) findViewById(R.id.plaintextEditView);
        plaintextTextView = (TextView) findViewById(R.id.plaintextTextView);
        ciphertextTextView = (TextView) findViewById(R.id.ciphertextTextView);
    }


    public void encryptButtonOnClick(View view) throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, IOException, NoSuchProviderException, InvalidKeyException, UnrecoverableEntryException, CertificateException, KeyStoreException {
        String plaintext = plaintextEditText.getText().toString();

        byte[] encryptedText = encryption.encrypt("MyAlias", plaintext);
        ciphertextTextView.setText(Base64.encodeToString(encryptedText, Base64.DEFAULT));
        System.out.println("The ciphertext is: " + Base64.encodeToString(encryptedText,Base64.DEFAULT) + " with length: " + encryptedText.length);

    }

    public void decryptButtonOnClick(View view) throws NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, IOException, KeyStoreException, IllegalBlockSizeException, CertificateException {
        System.out.println("The IV is: " + Base64.encodeToString(encryption.getIV(), Base64.DEFAULT) + " with length: " + encryption.getIV().length);
        String plaintext = decryption.decrypt("MyAlias", encryption.getEncryptedText(), encryption.getIV());
        plaintextTextView.setText(plaintext);
    }


}