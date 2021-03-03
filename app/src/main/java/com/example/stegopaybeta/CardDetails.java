package com.example.stegopaybeta;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.stegopaybeta.StegoPayUtils.BASE_URL;
import static com.example.stegopaybeta.StegoPayUtils.JWT_TOKEN;
import static com.example.stegopaybeta.StegoPayUtils.SHARED_PREF_NAME;
import static com.example.stegopaybeta.StegoPayUtils.getUnsafeOkHttpClient;

public class CardDetails extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), ViewCards.class);
        startActivity(i);
        //super.onBackPressed();
    }

    //Gallery Request Code, Storage permission code: used when user updates image of a card
    private final int GALLERY_REQUEST_CODE = 20;
    private final int STORAGE_PERMISSION_CODE = 10;

    //Initializing views of this activity
    private ImageView iv_coverImage;
    private TextView tv_nickName;
    private TextView tv_expiryDate;
    private ListView lv_activity;

    private FrameLayout frameLayout1;
    private TextView my_activity;
    private TextView view_all;
    private View rectangle_6;

    private ProgressBar progressBar;
    private TextView tv_progress;

    //To store the cardId (received as intent), JWTToken (retrieved from Shared Preferences)
    private String cardId;
    private String JWTToken;

    //Initializing retrofit interface
    Retrofit retrofit;
    StegoPayApi stegoPayApi;

    //To store userId (Will be retrieved from the JWTToken)
    private String userId;

    //Card Object
    Card card;

    //SQLite Helper object
    DataBaseHelper stegoPayDB;

    //Steganography object
    Steganography steganography;

    //To store decoded card details
    private String ccNumber;
    private String expiryDate;
    private String cvv;

    //List of transactions with the card being displayed
    ArrayList<Transaction> transactionsList;

    //Adapter to show list of transactions
    TransactionAdapter transactionAdapter;

    //Adapter to show list of transactions in a popup window (On click of view all)
    TransactionAdapterPopup transactionAdapter_popup;

    //To store the cover image
    Bitmap coverImage;

    //To check if image is updated
    boolean image_updated;

    //To check if key details (Card number, expiry, cvv) are updated
    boolean key_details_updated;

    //Initializing some of the update dialog views
    private ImageView iv_coverImage_update;
    private ProgressBar progressBar_update;
    private TextView tv_progress_update;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_details);

        //Initializing views and variables
        iv_coverImage = (ImageView) findViewById(R.id.iv_coverImage);
        tv_nickName = (TextView) findViewById(R.id.tv_nickName);
        tv_expiryDate = (TextView) findViewById(R.id.tv_expiryDate);
        lv_activity = (ListView) findViewById(R.id.lv_activity);

        frameLayout1 = (FrameLayout) findViewById(R.id.frameLayout1);
        my_activity = (TextView) findViewById(R.id.my_activity);
        view_all = (TextView) findViewById(R.id.view_all);
        rectangle_6 = findViewById(R.id.rectangle_6);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tv_progress = (TextView) findViewById(R.id.tv_progress);

        image_updated = false;
        key_details_updated = false;

        stegoPayDB = new DataBaseHelper(this);
        steganography = new Steganography();

        //Setting the progress bar text
        tv_progress.setText("Fetching \n Details");

        //Retrofit object
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build())
                .build();

        stegoPayApi = retrofit.create(StegoPayApi.class);

        //Getting cardId as intent
        Intent i = getIntent();
        cardId = i.getStringExtra("cardId");

        System.out.println("Card ID: " + cardId);

        //Getting JWTToken from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        //Hard coding it with the default value
        JWTToken = sharedPreferences.getString(JWT_TOKEN, "");

        //Decoding userId from JWTToken
        JWT jwt = new JWT(JWTToken);
        Claim claim = jwt.getClaim("_id");
        userId = claim.asString();

        //Updating JWTToken to make requests.
        JWTToken = "Bearer " + JWTToken;

        //Get card details
        getCardDetails();
    }

    //Get Card Details
    public void getCardDetails() {

        //Call to get card, specifying cardId as path, and JWT Token as Authorization header
        Call<Card> call = stegoPayApi.getCard(cardId, JWTToken);

        //Making request to /getCard/id
        call.enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {

                //If the response returned a error code
                if (!response.isSuccessful()) {
                    System.out.println("Response Code : " + response.code());

                    //Display a toast message
                    Toast.makeText(getApplicationContext(), "Unable to connect to the server, Please Try Again Later", Toast.LENGTH_LONG).show();

                    //Go back to list of views
                    finish();
                    return;
                }

                //If response was successful
                //Retrieve card details from response
                card = response.body();
                System.out.println("User id : " + userId);
                System.out.println("Card id : " + card.getCardID());

                System.out.println("CARD: " + card);

                //Decode Base64 ImageCard
                byte[] decodedImage = Base64.decode(card.getImage(), Base64.DEFAULT);

                //Convert byte[] to bitmap
                ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedImage);

                //Storing the bitmap in coverImage variable
                coverImage = BitmapFactory.decodeStream(inputStream);

                //Decode card
                String decodedBinary = steganography.decoding(card.getMappingKey(), coverImage);

                //Convert decoded binary to ASCII
                String decodedAscii = binaryToAscii(decodedBinary);

                //Get Card number, expiry, cvv from decoded ASCII
                String[] cardDetails = decodedAscii.split(" ");
                ccNumber = cardDetails[0];
                expiryDate = cardDetails[1];
                cvv = cardDetails[2];
                System.out.println("Decoded Ascii: " + decodedAscii);

                //Proceed to get transaction details
                getTransactions();
            }

            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to connect to the server, Please Try Again Later", Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }

    //Get transactions
    public void getTransactions() {

        //Call to get all transactions made by this card
        Call<ArrayList<Transaction>> call = stegoPayApi.getAllTransactionsOfACard(card.getCardID(), JWTToken);

        //Make request to /getAllTransactionsOfACard/cardId
        call.enqueue(new Callback<ArrayList<Transaction>>() {
            @Override
            public void onResponse(Call<ArrayList<Transaction>> call, Response<ArrayList<Transaction>> response) {
                if (!response.isSuccessful()) {
                    System.out.println(response.code());
                    Toast.makeText(getApplicationContext(), "Unable to connect to the server, Please Try Again Later", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                //Get the transactions returned into transactionsList
                transactionsList = response.body();

                //Proceed to setViews
                setViews();

            }

            @Override
            public void onFailure(Call<ArrayList<Transaction>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to connect to the server, Please Try Again Later", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    //Set Views
    public void setViews() {

        //Convert Bitmap (coverImage) to Drawable to set the background of the image view
        Drawable drawable = (Drawable) new BitmapDrawable(getResources(), coverImage);

        System.out.println("Image : " + coverImage);

        //Set nickName, expiry texts and image view
        tv_nickName.setText(card.getNickName());
        tv_expiryDate.setText(expiryDate);
        iv_coverImage.setBackground(drawable);

        //Initialize transaction adapter which appears on the screen, include only 3 transactions.
        if(transactionsList.size()>=3) {
            transactionAdapter = new TransactionAdapter(this, (ArrayList<Transaction>) transactionsList.subList(0, 3));
        }
        //If the total number of transactions are less than 3, then pass the entire list
        else{
            transactionAdapter = new TransactionAdapter(this,transactionsList);
        }

        //Set Adapter for the list view
        lv_activity.setAdapter(transactionAdapter);

        //Hide progress bar and display all the views
        hideProgressBar();

    }

    //On click of View all transactions
    public void viewAllTransactions(View v) {
        //Initializing dialog
        final Dialog dialog = new Dialog(this);

        //Set view of dialog to the custom layout made, which includes a list view
        dialog.setContentView(R.layout.custom_dialog_transactions_list);

        //Set title
        dialog.setTitle("Transactions");

        //initializing list view in the custom layout. (View in custom_dialog_transactions_list.xml)
        ListView pop_up_list_view = (ListView) dialog.findViewById(R.id.custom_dialog_listView_transactions);

        //Initializing adapter for pop up list view
        transactionAdapter_popup = new TransactionAdapterPopup(this, transactionsList);

        //Setting the adapter
        pop_up_list_view.setAdapter(transactionAdapter_popup);

        //Specifying dialog width and height
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, 1000);

        //Display the dialog with the list view
        dialog.show();
    }

    //On Click of Update Card
    public void updateCard(View v) {

        //Update card dialog Views (Views in update_card_dialog.xml)
        EditText et_nickName_update;
        EditText et_ccNumber_update;
        EditText et_expiry_update;
        EditText et_cvv_update;
        Button bt_update_cover_image;

        //Initializing alert dialog, with update_card_dialog as its view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.update_card_dialog, null);

        //Initializing the update_card_dialog views
        et_nickName_update = (EditText) view.findViewById(R.id.et_nickName_update);
        et_ccNumber_update = (EditText) view.findViewById(R.id.et_ccNumber_update);
        et_expiry_update = (EditText) view.findViewById(R.id.et_expiry_update);
        et_cvv_update = (EditText) view.findViewById(R.id.et_cvv_update);
        bt_update_cover_image = (Button) view.findViewById(R.id.bt_update_cover_image);
        iv_coverImage_update = (ImageView) view.findViewById(R.id.iv_coverImage_update);
        progressBar_update = (ProgressBar) view.findViewById(R.id.progressBar_update);
        tv_progress_update = (TextView) view.findViewById(R.id.tv_progress_update);

        //Setting the views to display the current card details
        et_nickName_update.setText(card.getNickName());
        et_ccNumber_update.setText(ccNumber);
        et_expiry_update.setText(expiryDate);
        et_cvv_update.setText(cvv);
        iv_coverImage_update.setImageBitmap(coverImage);

        builder.setView(view)
                //Setting title of the alert dialog
                .setTitle("Update Card")

                //On Click of update button on the dialog
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //On Click of update
                        //Progress bar to display updating

                        //If card number, expiry, cvv were changed
                        if (et_ccNumber_update.getText().toString() != ccNumber || et_expiry_update.getText().toString() != expiryDate || et_cvv_update.getText().toString() != cvv) {

                            //Set key details updated to true
                            key_details_updated = true;
                        }

                        //If key details were updated or image was updated, then need to remap the details
                        if (key_details_updated || image_updated) {

                            //If image is updated
                            if(image_updated){
                                //If image was updated set the hashmap 1 to the new hashmap 1
                                card.setHashMap_1(steganography.getHashMap_1());

                                //Set the cover image to the new selected image bitmap
                                coverImage = steganography.getImage();
                            }

                            //Store updated details in the variables
                            ccNumber = et_ccNumber_update.getText().toString();
                            expiryDate = et_expiry_update.getText().toString();
                            cvv = et_cvv_update.getText().toString();

                            //Get updated details in binary
                            String details = ccNumber + " " + expiryDate + " " + cvv;
                            String detailsInBinary = StringconvertToBinary(details);

                            System.out.println(card.getHashMap_1());

                            //Map the updated details
                            String updatedMappingKey = steganography.single_pattern_mapping(detailsInBinary, coverImage, card.getHashMap_1());

                            //Set mapping key of the card object
                            card.setMappingKey(updatedMappingKey);

                            //Set the last 4 digits
                            card.setLast4Digits(ccNumber.substring(ccNumber.length() - 4));

                            //Set the updated cover image (By converting it to a Base64 string)
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            coverImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] coverImageByte = byteArrayOutputStream.toByteArray();
                            String image_base64 = Base64.encodeToString(coverImageByte, Base64.DEFAULT);

                            card.setImage(image_base64);
                        }

                        //Set the nickname
                        card.setNickName(et_nickName_update.getText().toString());

                        //Update details in MongoDB, and SQLite DB
                        updateMongo();
                        //updateSQLITE();

                        //Set variables back to default values
                        image_updated = false;
                        key_details_updated = false;
                        steganography.setImage(null);

                        //Set the updated views
                       // setViews();

//                        //Hide the update_dialog progress bar
                    }
                })
                //On click of cancel button on the dialog
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        //Display the dialog
        builder.show();

        //On Click of update cover image
        bt_update_cover_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go to update cover image
                updateCoverImage();
            }
        });

    }

    //Update mongo
    public void updateMongo() {
        Call<Card> call = stegoPayApi.updateCard(card.getCardID(), JWTToken, card);
        tv_progress.setText("Updating");
        tv_progress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);


        call.enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Unable to connect to server : " + response.code(), Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Card Updated in Mongo", Toast.LENGTH_LONG).show();
                updateSQLITE();
            }

            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to connect to server2 : " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    public void updateSQLITE() {
        boolean result = stegoPayDB.updateCard(userId, card.getCardID(), card.getNickName(), card.getImage(), convertHashMapToString(card.getHashMap_1()), card.getLast4Digits());
        if (result) {
        Toast.makeText(getApplicationContext(), "Card Updated in SQLite", Toast.LENGTH_LONG).show();
        setViews();
        progressBar.setVisibility(View.GONE);
        tv_progress.setVisibility(View.GONE);
        } else {
            Toast.makeText(getApplicationContext(), "Error updating card in SQLite", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //On click of delete card button
    public void deleteCard(View v) {
        //Display a alert dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                //Set title
                .setTitle("Delete Card")
                //Set message
                .setMessage("Are you sure you want to delete the card : ******" + card.getLast4Digits())
                //On Click of yes
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Delete card from mongo db and sqlite db
                        deleteFromMongo();
                    }
                })
                //On Click of cancel do nothing
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void deleteFromMongo() {
        Call<JsonObject> call = stegoPayApi.deleteCard(card.getCardID(), JWTToken);

        progressBar.setVisibility(View.VISIBLE);
        tv_progress.setText("Deleting");
        tv_progress.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Unable to connect to server: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Card Deleted From Mongo: "+response.body().get("success"),Toast.LENGTH_LONG).show();
                deleteFromSQLite();

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to connect to server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

    public void deleteFromSQLite() {
        boolean success = stegoPayDB.deleteCard(userId,card.getCardID());
        if(success){
            tv_progress.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(), "Card Deleted From SQLITE",Toast.LENGTH_LONG).show();
        finish();
        }
        else{
            Toast.makeText(getApplicationContext(),"ERROR DELETING CARD FROM SQLITE",Toast.LENGTH_LONG).show();
        }
    }

    public void hideProgressBar() {

        //Progress bar and it's text views are hidden
        progressBar.setVisibility(View.INVISIBLE);
        tv_progress.setVisibility(View.INVISIBLE);

        //Rest of the views are shown
        frameLayout1.setVisibility(View.VISIBLE);
        my_activity.setVisibility(View.VISIBLE);
        view_all.setVisibility(View.VISIBLE);
        rectangle_6.setVisibility(View.VISIBLE);
        lv_activity.setVisibility(View.VISIBLE);

    }

    public void hideUpdateProgressBar() {
        //Hide progress bar belonging to the update pop up window
        progressBar_update.setVisibility(View.INVISIBLE);
        tv_progress_update.setVisibility(View.INVISIBLE);
    }

    public void showUpdateProgressBar() {
        //Display progress bar belonging to the update pop up window
        progressBar_update.setVisibility(View.VISIBLE);
        tv_progress_update.setVisibility(View.VISIBLE);
    }

    //Converting binary data to ASCII
    public String binaryToAscii(String binary) {
        //To store ascii
        String ascii = "";

        //length of the binary data
        int length = binary.length();

        //Going through the binary string
        for (int i = 0; i < length; i = i + 8) {
            //Get 8 bits
            String firstChar = binary.substring(i, i + 8);
            //Convert to ASCII
            char c = (char) Integer.parseInt(firstChar, 2);
            //Append to the string
            ascii += c;
        }
        //Return ASCII
        return ascii;
    }

    //Converting a hashmap to string
    public String convertHashMapToString(HashMap toConvert) {
        String converted = "";
        Iterator hmIterator = toConvert.entrySet().iterator();

        while (hmIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) hmIterator.next();
            converted += mapElement.getKey() + ":" + mapElement.getValue() + ";";
        }
        return converted;
    }

    //OnClick of button to update cover image
    public void updateCoverImage() {
        //If storage permission isnt provided, then get storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

        } else {
            requestStoragePermission();
        }
        //Intent to go to gallery activity
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);
    }

    //Request storage permission from the user
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(CardDetails.this,
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

    //On activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Coming back from Gallery
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

    //Preprocessing AsyncTask
    private class preprocessing extends AsyncTask<Bitmap, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Show progress bar (on top of the dialog) before executing the background thread
            tv_progress_update.setText("Preprocessing");
            showUpdateProgressBar();
        }

        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            //Set image for steganographyObj
            steganography.setImage(bitmaps[0]);

            //Start Preprocessing
            steganography.preProcessing2();

            //If the image selected is StegoPay ready
            if (steganography.check_image_validity()) {
                //Set image updated to true
                image_updated = true;

                //Return true
                return true;
            } else {
                //Set image to null
                steganography.setImage(null);
                //Set image updated to false
                image_updated = false;
                //Return false
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //Dismissing the progress bar
            hideUpdateProgressBar();

            //If preprocessing returns true
            if (aBoolean == true) {
                //Set cover image on the update dialog
                iv_coverImage_update.setImageBitmap(steganography.getImage());
            }
            else {
                //Else set the image on the update dialog to the original image used.
                iv_coverImage_update.setImageBitmap(coverImage);

                //Display a dialog to inform the user, and allow him to select another image
                new AlertDialog.Builder(CardDetails.this)
                        .setTitle("Image Invalid")
                        .setMessage("The image selected isn't stegoPay ready")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }

        }
    }
    //To convert a string to binary
    public String StringconvertToBinary(String input) {
        String binary = "";

        int length = input.length();

        //Go through the input string
        for (int i = 0; i < length; i++) {
            //Convert each character to decimal
            int val = Integer.valueOf(input.charAt(i));

            //Convert decimal to binary string
            String temp = Integer.toBinaryString(val);

            //Format to 8 bits
            temp = String.format("%8s", temp).replace(' ', '0');

            //Append to the binary string
            binary += temp;

        }
        //Return the binary string
        return binary;
    }



}
