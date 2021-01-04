package com.example.stegopaybeta;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    private byte[] encryptedResult;
    private byte[] ciphertext;
    private byte[] authTag;
    private byte[] IV;
    private byte[] AAD;
    private SecretKey preSharedKey;
    private KeyStore keyStore;
    private String ENCRYPTION_KEY  = "0123456789abcdef";
    private SecureRandom secureRandom;


    public Encryption() {
    }

    public byte[] encrypt(String plainText) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, CertificateException, KeyStoreException, UnrecoverableEntryException {

//        keyStore = KeyStore.getInstance("AndroidKeyStore");
//        keyStore.load(null);

        // Generates AES key and stores it in AndroidKeyStore if a key entry does not exist in the keyStore
//        if(keyStore.containsAlias(alias) == false) {
//            generateKey(alias);
//        }

        // Initializing the cipher to be used, AES galois counter mode with no padding
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        //Converting encryption key to secret key object by getting its bytes
        preSharedKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, preSharedKey);

        // Getting a random 12 bytes IV
        IV = cipher.getIV();

        //AAD = new SecureRandom().generateSeed(16);

        //cipher.updateAAD(AAD);

        // Performing the encryption
         encryptedResult = cipher.doFinal(plainText.getBytes("UTF-8"));

         //Separating the cipher text and authentication tag from the encrypted result
         ciphertext = Arrays.copyOfRange(encryptedResult,0, encryptedResult.length - 16);
         authTag = Arrays.copyOfRange(encryptedResult, encryptedResult.length - 16, encryptedResult.length);

        return ciphertext;

        //cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(alias));


    }

    private void generateKey(String alias) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException, UnrecoverableEntryException {

//        keyStore = KeyStore.getInstance("AndroidKeyStore");
//        keyStore.load(null);

      //  if (!keyStore.containsAlias(alias)) {

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());

            keyGenerator.generateKey();
   // }
}

     private Key getSecretKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        keyStore = keyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return keyStore.getKey(alias, null);
     }


    public byte[] getIV() {
        return IV;
    }

    public byte[] getAuthTag() { return authTag; }

    public byte[] getAAD() { return AAD; }

    public byte[] getEncryptedResult() { return encryptedResult; }

    public SecretKey getPreSharedKey() { return preSharedKey; }




}
