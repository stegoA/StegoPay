package com.example.stegopaybeta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.amygdalum.stringsearchalgorithms.search.StringFinder;
import net.amygdalum.stringsearchalgorithms.search.StringMatch;
import net.amygdalum.stringsearchalgorithms.search.chars.AhoCorasick;
import net.amygdalum.stringsearchalgorithms.search.chars.Horspool;
import net.amygdalum.util.io.CharProvider;
import net.amygdalum.util.io.StringCharProvider;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static java.util.Arrays.asList;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.LONGEST_MATCH;
import static net.amygdalum.stringsearchalgorithms.search.MatchOption.NON_OVERLAP;

public class AddCard extends AppCompatActivity {

    //gallery request code
    private static final int GALLERY_REQUEST_CODE = 123;

    //to store the cover image selected
    Bitmap coverImage;

    //to store the mapping key generated
    String mappingKey;

    // Views
    Button encodeButton;
    EditText ccnEditText, expDateEditText, cvvEditText, amountEditText;
    ImageView imageView;

    //to store the timestamp generated
    String timestamp;

    //to store the encrypted details (credit card number + expiry date + cvv + amount + timestamp)
    byte[] encryptedText;

    //Encryption, decryption, Add card objects
    Encryption encryption;
    Decryption decryption;
    AddCard obj;

    //steganography object
    Steganography steganographyObj = new Steganography();

    //storage permission code
    private int STORAGE_PERMISSION_CODE = 1;

    //Progress dialog to be displayed when mapping and preprocessing is going on
    ProgressDialog progressDialog_preprocessing;
    ProgressDialog progressDialog_mapping;

    //boolean to check if a valid image is selected
    boolean valid_image_selected;

    //Values to initialize timer varibles to calculate execution times (Preprocessing and mapping)
    long starttime_preprocessing = 0;
    long endtime_preprocessing = 0;

    long startTime_Mapping = 0;
    long endTime_Mapping = 0;

    //Handler for getting the mapping key from the background thread for mapping
    Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_card);

        //Instantiating encryption, decryption, and addcard objects.
        encryption = new Encryption();
        decryption = new Decryption();
        obj = new AddCard();

        //Instantiating progress dialog objects
        progressDialog_preprocessing = new ProgressDialog(this);
        progressDialog_mapping = new ProgressDialog(this);

        //Setting valid_image_selected to false by default
        valid_image_selected = false;


        // Initializing views
        ccnEditText = (EditText) findViewById(R.id.ccnEditText);
        expDateEditText = (EditText) findViewById(R.id.expDateEditText);
        cvvEditText = (EditText) findViewById(R.id.cvvEditText);
        amountEditText = (EditText) findViewById(R.id.amountEditText);
        imageView = (ImageView) findViewById(R.id.imageView);
        encodeButton = (Button) findViewById(R.id.encodeButton);

        //Setting progress bar properties
        progressDialog_preprocessing.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog_preprocessing.setTitle("Preprocessing");
        progressDialog_preprocessing.setMessage("Please wait while the image is being processed, This operation may take a while");
        progressDialog_preprocessing.setIndeterminate(true);
        progressDialog_preprocessing.setCanceledOnTouchOutside(false);

        progressDialog_mapping.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog_mapping.setTitle("Mapping");
        progressDialog_mapping.setIndeterminate(true);
        progressDialog_mapping.setCancelable(false);

        //On click listener for encode button
        encodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If a image is selected, and the image is stego pay ready
                if (valid_image_selected) {

                    System.out.println("Image is StegoPay Ready");
                    System.out.println("Before converting ciphertext to binary: " + Base64.encodeToString(encryptedText, Base64.DEFAULT) + " with length: " + encryptedText.length);

                    //Converting encrypted byte array to binary
                    String encryptedCCDetailsBinaryString = byteConvertToBinary(encryptedText);

                    System.out.println("CCDetailsBinary : " + encryptedCCDetailsBinaryString);
                    System.out.println("Number of Bits : " + encryptedCCDetailsBinaryString.length());

                    //Showing the progress bar for mapping
                    progressDialog_mapping.show();

                    //Starting to map, with single pattern matching algorithm, done in the background thread
                    new Thread(new mapping_background(encryptedCCDetailsBinaryString)).start();
                } else {
                    System.out.println("Image is not StegoPay Ready");
                    //Alert dialog to inform the user, that a image isnt selected
                    new AlertDialog.Builder(AddCard.this)
                            .setTitle("Image Not Selected")
                            .setMessage("A valid cover image isn't selected yet.")
                            .setPositiveButton("Select Image", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //to go to gallery to select an image
                                    selectCoverImageButtonOnClick(null);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }

            }
        });

        //Handler to handler returned message object from the background thread created for mapping
        myHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                //Dismissing the progress dialog as mapping is done
                progressDialog_mapping.dismiss();

                //Setting the mapping key variable to the mapping key returned
                mappingKey = (String) msg.obj;

                //Mapping key generated
                System.out.println("Mapping Key : " + mappingKey);

                //Printing the number of mappings generated
                String[] mappings = mappingKey.split(";");
                System.out.println("Number of mappings : " + mappings.length);

                //Printing execution time
                long durationInMillis_mapping = TimeUnit.NANOSECONDS.toMillis((endTime_Mapping - startTime_Mapping));
                System.out.println("Mapping in millis : " + durationInMillis_mapping);

            }
        };

    }


    //Converting a byte array to binary string
    public String byteConvertToBinary(byte[] input) {
        String binaryString = "";

        for (byte b : input) {
            String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            binaryString += s1;
        }

        return binaryString;
    }

    //on click of button to generate timestamp
    public void generateTimestamp(View view) {
        //Generating time stamp of the format dd-MM-yyyy-hh-mm-ss
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");

        //Storing timestamp
        timestamp = simpleDateFormat.format(new Date());
        System.out.println("Timestamp: " + timestamp);
    }

    //on click of encrypt button
    public void encryptButtonOnClick(View view) throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, IOException, NoSuchProviderException, InvalidKeyException, UnrecoverableEntryException, CertificateException, KeyStoreException {

        String space = " ";

        //Get credit card details entered by the user
        String creditCardNumber = ccnEditText.getText().toString();
        String expDate = expDateEditText.getText().toString();
        String CVV = cvvEditText.getText().toString();
        String amount = amountEditText.getText().toString();

        //Store all the details as a single string to encrypt
        String ccDetails = creditCardNumber + space + CVV + space + expDate + space + amount + space + timestamp;

        System.out.println("The plaintext is: " + ccDetails + " with length: " + ccDetails.length());

        //Encrypt credit card details, returns a byte array
        encryptedText = encryption.encrypt(ccDetails);

        Toast.makeText(AddCard.this, "Encryption successful!", Toast.LENGTH_SHORT).show();

        //Printing the entire encrypted result with Base64 encoding
        System.out.println("Encrypted result (ciphertext + authTag): " + Base64.encodeToString(encryption.getEncryptedResult(), Base64.DEFAULT) + " with length: " + encryption.getEncryptedResult().length);
        //Printing the cipher text part of encrypted result
        System.out.println("Ciphertext: " + Base64.encodeToString(encryptedText, Base64.DEFAULT) + " with length: " + encryptedText.length);
        //Printing the authentication tag
        System.out.println("Authentication tag: " + Base64.encodeToString(encryption.getAuthTag(), Base64.DEFAULT) + " with length: " + encryption.getAuthTag().length);

    }

    //OnClick of button to select cover image
    public void selectCoverImageButtonOnClick(View view) {

        //If storage permission isnt provided, then get storage permission
        if (ContextCompat.checkSelfPermission(AddCard.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(AddCard.this, "You have already granted this permission!",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
        }

        //Intent to go to gallery activity
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);

    }

    //On click of button to send data to the server
    public void sendButtonOnClick(View view) {
        //Executing sendData AsyncTask
        sendData sendData = new sendData();
        sendData.execute();
    }


    //Receiving the image selected by the user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //to store image selected as a bitmap
            Bitmap temp = null;
            try {
                //Get image uri
                Uri imageData = data.getData();
                //Convert to bitmap
                InputStream imageStream = getContentResolver().openInputStream(imageData);
                temp = BitmapFactory.decodeStream(imageStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Execute AsyncTask for preprocessing the image
            new preprocessing().execute(temp);
        }
    }

    //Request storage permission from the user
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(AddCard.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Send Data AsyncTask
    class sendData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            //Server IP
            String IP = "10.0.2.2";

            //Server port number
            int serverPortNumber = 9999;

            try {
                //Establishing connection
                Socket socket = new Socket(IP, serverPortNumber);

                //Converting cover image to a byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                coverImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                //Sending the cover image, mapping key, IV, and authentication tag to the server
                OutputStream socketOutputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(socketOutputStream);
                dataOutputStream.writeInt(byteArray.length);
                dataOutputStream.write(byteArray, 0, byteArray.length);
                dataOutputStream.flush();

                dataOutputStream.writeUTF(mappingKey);
                dataOutputStream.flush();

                dataOutputStream.writeInt(encryption.getIV().length);
                dataOutputStream.write(encryption.getIV(), 0, encryption.getIV().length);
                dataOutputStream.flush();

                dataOutputStream.writeInt(encryption.getAuthTag().length);
                dataOutputStream.write(encryption.getAuthTag(), 0, encryption.getAuthTag().length);
                dataOutputStream.flush();

                //Closing the output streams and the socket
                dataOutputStream.close();
                socketOutputStream.close();
                socket.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    //Preprocessing AsyncTask
    private class preprocessing extends AsyncTask<Bitmap, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Show progress dialog before executing the background thread
            progressDialog_preprocessing.show();
        }

        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            //Set image for steganographyObj
            steganographyObj.setImage(bitmaps[0]);

            starttime_preprocessing = System.nanoTime();
            //Start Preprocessing
            steganographyObj.preProcessing2();
            endtime_preprocessing = System.nanoTime();

            //If the image selected is StegoPay ready
            if (steganographyObj.check_image_validity()) {
                //Set coverImage to the selected image
                coverImage = bitmaps[0];

                //Set valid image selected boolean to true
                valid_image_selected = true;

                //Return true
                return true;
            } else {
                //Set image to null
                steganographyObj.setImage(null);
                //Set valid image selected boolean to false
                valid_image_selected = false;
                //Return false
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //Dismissing the progress dialog
            progressDialog_preprocessing.dismiss();

            //Displaying preprocessing execution time
            long durationInMillis_preprocessing = TimeUnit.NANOSECONDS.toMillis((endtime_preprocessing - starttime_preprocessing));
            System.out.println("Pre processing time : " + durationInMillis_preprocessing);

            //If image selected was stegoPay ready
            if (aBoolean == true) {
                //Set image view to image selected
                imageView.setImageBitmap(coverImage);
            } else {
                //Set image view to null (no picture)
                imageView.setImageBitmap(null);
                //Display a dialog to inform the user, and allow him to select another image
                new AlertDialog.Builder(AddCard.this)
                        .setTitle("Image Invalid")
                        .setMessage("The image selected isn't stegoPay ready, try selecting a picture with more colors")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectCoverImageButtonOnClick(null);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        }
    }

    //Runnable to execute mapping operation
    private class mapping_background implements Runnable {

        private String encryptedCCDetailsBinary;

        //Default constructor to get encryted credit card details in binary
        mapping_background(String encryptedCCDetailsBinary) {
            this.encryptedCCDetailsBinary = encryptedCCDetailsBinary;
        }

        //Method to be executed in the background thread
        @Override
        public void run() {
            //to store the mapping key
            String mapping_key;

            startTime_Mapping = System.nanoTime();
            //Start mapping, and get the mapping key
            mapping_key = steganographyObj.single_pattern_mapping(encryptedCCDetailsBinary);
            endTime_Mapping = System.nanoTime();

            //Creating a message instance
            Message msg = Message.obtain();
            //Putting the string into the object field of the message
            msg.obj = mapping_key;
            //Setting the target to send the result to (Handler)
            msg.setTarget(myHandler);
            //Sending the mapping key to the handler(which will run on the main thread)
            msg.sendToTarget();
        }
    }


/* Method unused
    public String getDetailsInBinary(String encryptedCCDetails) {
        String encryptedCCDetailsBinary = StringconvertToBinary(encryptedCCDetails);
        return encryptedCCDetailsBinary;
    }*/
/* Method unused
    public static byte[] getByteByString(String binaryString) {
        int splitSize = 8;

        if (binaryString.length() % splitSize == 0) {
            int index = 0;
            int position = 0;

            byte[] resultByteArray = new byte[binaryString.length() / splitSize];
            StringBuilder text = new StringBuilder(binaryString);

            while (index < text.length()) {
                String binaryStringChunk = text.substring(index, Math.min(index + splitSize, text.length()));
                Integer byteAsInt = Integer.parseInt(binaryStringChunk, 2);
                resultByteArray[position] = byteAsInt.byteValue();
                index += splitSize;
                position++;
            }
            return resultByteArray;
        } else {
            System.out.println("Cannot convert binary string to byte[], because of the input length. '" + binaryString + "' % 8 != 0");
            return null;
        }
    }*/
/* Method unused
    public String StringconvertToBinary(String input) {
        String binary = "";

        int length = input.length();

        for (int i = 0; i < length; i++) {
            //ASCII
            int val = Integer.valueOf(input.charAt(i));

            String temp = Integer.toBinaryString(val);

            temp = String.format("%8s", temp).replace(' ', '0');
            binary += temp;

        }
        return binary;
    }*/
    //    public void decryptButtonOnClick(View view) throws NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, IOException, KeyStoreException, IllegalBlockSizeException, CertificateException {
//        System.out.println("The IV is: " + Base64.encodeToString(encryption.getIV(), Base64.DEFAULT) + " with length: " + encryption.getIV().length);
//        String plaintext = decryption.decrypt("MyAlias", encryption.getEncryptedText(), encryption.getIV());
//        plaintextTextView.setText(plaintext);
//    }


}