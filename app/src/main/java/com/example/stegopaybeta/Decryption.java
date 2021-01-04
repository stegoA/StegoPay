package com.example.stegopaybeta;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Decryption {

    private KeyStore keyStore;
    private byte[] decryptedText;
    private String ENCRYPTION_KEY  = "0123456789abcdef";

    public Decryption() {
    }


    public String decrypt(byte[] encryptedText, SecretKey preSharedKey, byte[] IV, byte[] authTag) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException {

       // initKeyStore();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, IV);
        cipher.init(Cipher.DECRYPT_MODE, preSharedKey, gcmSpec);
        //cipher.updateAAD(AAD);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(encryptedText);
        outputStream.write(authTag);

        byte encryptedTextFinal[] = outputStream.toByteArray();

        decryptedText = cipher.doFinal(encryptedTextFinal);

        return new String (decryptedText, "UTF-8");
    }

    public Key getSecretKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        keyStore = keyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return keyStore.getKey(alias, null);
    }




}
