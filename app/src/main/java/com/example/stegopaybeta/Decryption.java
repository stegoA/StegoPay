package com.example.stegopaybeta;

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

public class Decryption {

    private KeyStore keyStore;
    private byte[] decryptedText;

    public Decryption() {
    }


    public String decrypt(String alias, byte[] encryptedText, byte[] IV) throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException, CertificateException {

       // initKeyStore();

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(alias), new GCMParameterSpec(128, IV));

        decryptedText = cipher.doFinal(encryptedText);

        return new String (decryptedText, "UTF-8");
    }

    private Key getSecretKey(String alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        keyStore = keyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return keyStore.getKey(alias, null);
    }

}
