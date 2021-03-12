 package com.example.stegopaybeta;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.stegopaybeta.CreditCardUtils.getCardType;
import static com.example.stegopaybeta.StegoPayUtils.BASE_URL;
import static com.example.stegopaybeta.StegoPayUtils.JWT_TOKEN;
import static com.example.stegopaybeta.StegoPayUtils.SHARED_PREF_NAME;
import static com.example.stegopaybeta.StegoPayUtils.getUnsafeOkHttpClient;
import static com.example.stegopaybeta.StegoPayUtils.getUserIDFromToken;

public class AddCard extends AppCompatActivity {


    SharedPreferences sharedPreferences;

    DataBaseHelper db;

    Retrofit retrofit;

    // Gallery request code
    private static final int GALLERY_REQUEST_CODE = 123;

    // Storage permission code
    private int STORAGE_PERMISSION_CODE = 1;

    private final int VIEW_CARDS_REQUEST_CODE = 10;

    // Object declaration
    AddCard addCardObj;
    Steganography stegoObj;
    CreditCardUtils creditCardUtilsObj;

    // To store the cover image selected
    Bitmap coverImage;

    // To store the image data
    Uri imageData;

    // To store the mapping key generated
    String mappingKey;

    // Views
    Button selectCoverImageButton, addCardButton;
    EditText ccnEditText, expDateEditText, cvvEditText, cardNickNameEditText;
    TextView selectedImageNameTextView;
    ImageView masterCardImageView, visaCardImageView;
    ProgressBar progressBar;


    // ProgressDialog to be displayed when mapping and pre-processing has started
    ProgressDialog progressDialog_preprocessing;
    ProgressDialog progressDialog_mapping;

    // Boolean variable to check if a valid cover image has been selected
    boolean valid_image_selected;

    // Boolean variable for credit card form validation
    boolean isDelete;


    // Handler for getting the mapping key from the background thread for mapping
    Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_card_final);

         retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getUnsafeOkHttpClient().build())
                .build();

        // Objects instantiation
        addCardObj = new AddCard();
        stegoObj = new Steganography();
        creditCardUtilsObj = new CreditCardUtils();


         // Initializing views
        ccnEditText = (EditText) findViewById(R.id.ccnEditText);
        expDateEditText = (EditText) findViewById(R.id.expDateEditText);
        cvvEditText = (EditText) findViewById(R.id.cvvEditText);
        selectCoverImageButton = (Button) findViewById(R.id.selectCoverImageButton);
        addCardButton = (Button) findViewById(R.id.addCardButton);
        selectedImageNameTextView = (TextView) findViewById(R.id.selectedImageNameTextView);
        masterCardImageView = (ImageView) findViewById(R.id.masterCardImageView);
        visaCardImageView = (ImageView) findViewById(R.id.visaCardImageView);
        cardNickNameEditText = (EditText) findViewById(R.id.cardNickNameEditText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        // Instantiating progress dialog objects
        progressDialog_preprocessing = new ProgressDialog(this);
        progressDialog_mapping = new ProgressDialog(this);

        // Setting valid_image_selected to false by default
        valid_image_selected = false;

        // Setting progress bar properties
        progressDialog_preprocessing.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog_preprocessing.setTitle("Pre-processing");
        progressDialog_preprocessing.setMessage("Please wait while the image is being processed. This operation may take a while.");
        progressDialog_preprocessing.setIndeterminate(true);
        progressDialog_preprocessing.setCanceledOnTouchOutside(false);

        progressDialog_mapping.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog_mapping.setTitle("Mapping");
        progressDialog_mapping.setIndeterminate(true);
        progressDialog_mapping.setCancelable(false);

        // Credit card form validation (credit card number edit text)
        ccnEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before==0) {
                    isDelete = false;
                }

                else {
                    isDelete = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String sourceText = s.toString();

                int length = sourceText.length();

                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append(sourceText);


                if(length >= 2 && masterCardImageView!=null && visaCardImageView != null) {
                    int cardType = getCardType(sourceText.trim());

                    switch (cardType) {
                        case 1:
                            visaCardImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_visa, getApplicationContext().getTheme()));
                            break;
                        case 2:
                            masterCardImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_mastercard, getApplicationContext().getTheme()));
                            break;
                    }

                }

                else {
                    visaCardImageView.setImageResource(android.R.color.transparent);
                    masterCardImageView.setImageResource(android.R.color.transparent);
                }

                if (length > 0 && length % 5 == 0) {

                    if (isDelete) {
                        stringBuilder.deleteCharAt(length - 1);
                    }

                    else {
                        stringBuilder.insert(length - 1, " ");
                    }

                    ccnEditText.setText(stringBuilder);

                    ccnEditText.setSelection(ccnEditText.getText().length());


                }
            }
        });

        // Credit card form validation (expiration date edit text)
        expDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //Log.d("TAG", "onTextChanged: " + before);


                if (before==0) {
                    isDelete = false;

                }

                else {
                    isDelete = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String source = s.toString();
                int length = source.length();

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(source);

                if (length == 2) {
                    if (isDelete) {
                        stringBuilder.deleteCharAt(length - 1);
                    } else {
                        stringBuilder.append("/");
                    }
                    expDateEditText.setText(stringBuilder);
                    expDateEditText.setSelection(expDateEditText.getText().length());
                }

            }
        });




        selectCoverImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCoverImageButtonOnClick();
            }
        });


        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addCardButtonOnClick();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        // Has to be created already (User USER_ID table)
        // Sample creation
        db = new DataBaseHelper(this);



    }


    public String getTokenFromSharedPrefs() {

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);

        String fromSharedPrefs = sharedPreferences.getString(JWT_TOKEN, "");

        System.out.println("The token I got from sharedPrefs is: " + fromSharedPrefs);

        return fromSharedPrefs;

    }

    public void selectCoverImageButtonOnClick() {

        // If storage permission is not provided, then get storage permission
        if (ContextCompat.checkSelfPermission(AddCard.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                 requestStoragePermission();
        }

        // Intent to go to gallery activity
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);

    }



    public void addCardButtonOnClick() throws InterruptedException {

        progressBar.setVisibility(View.VISIBLE);


        //Handler to handle returned message object from the background thread created for mapping
        myHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                //Dismissing the progress dialog as mapping is done
                progressDialog_mapping.dismiss();

                //Setting the mapping key variable to the mapping key returned
                mappingKey = (String) msg.obj;

                //Mapping key generated
                //System.out.println("Mapping Key : " + mappingKey);

                //Printing the number of mappings generated
                //String[] mappings = mappingKey.split(";");
                //System.out.println("Number of mappings : " + mappings.length);

                createCard(mappingKey);

            }
        };

        if(ccnEditText.getText().toString().trim().replaceAll("\\s", "").length() != 16) {
            ccnEditText.setError("Enter your credit card number.");
            ccnEditText.requestFocus();
            progressBar.setVisibility(View.GONE);
        }

        if(expDateEditText.getText().toString().trim().length() != 5) {
            expDateEditText.setError("Enter the expiry date.");
            expDateEditText.requestFocus();
            progressBar.setVisibility(View.GONE);
        }

        if(cvvEditText.getText().toString().trim().length() != 3) {
            cvvEditText.setError("Enter the CVV.");
            cvvEditText.requestFocus();
            progressBar.setVisibility(View.GONE);
        }

        if(cardNickNameEditText.getText().toString().trim().length() == 0) {
            cardNickNameEditText.setError("Give your card a nickname.");
            cardNickNameEditText.requestFocus();
            progressBar.setVisibility(View.GONE);
        }

        // Check if an image is selected and is StegoPay ready
        if (valid_image_selected && ccnEditText.getText().toString().trim().replaceAll("\\s", "").length() == 16 && expDateEditText.getText().toString().trim().length() == 5 && cvvEditText.getText().toString().trim().length() == 3 && cardNickNameEditText.getText().toString().trim().length() > 0) {

            // Get card details entered by the user
            String ccDetails = getCardData();

            System.out.println("Image is StegoPay Ready");

            // Converting ccDetails to a binary string
            String ccDetailsBinaryString = stringConvertToBinary(ccDetails);

            System.out.println("CCDetailsBinary : " + ccDetailsBinaryString);
            System.out.println("Number of Bits : " + ccDetailsBinaryString.length());

            // Showing the progress bar for mapping
            progressDialog_mapping.show();

            // Starting to map, with single pattern matching algorithm, done in the background thread
            Thread thread1 = new Thread(new mapping_background(ccDetailsBinaryString));
            thread1.start();
            thread1.join();

            // new Thread(new mapping_background(ccDetailsBinaryString)).start();

        } else {
            progressBar.setVisibility(View.GONE);
            //Alert dialog to inform the user, that a image isnt selected
            new AlertDialog.Builder(AddCard.this)
                    .setTitle("Fill the form!")
                    .setMessage("You have not completed the form yet.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }


    }

    public void createCard(String mappingKey) {

        String ccDetails = getCardData();

        String last4Digits = ccDetails.substring(12, 16);
        String nickName = cardNickNameEditText.getText().toString();

        byte[] coverImageBytes = convertBitmapToByteArray();
        String coverImageEncoded = Base64.encodeToString(coverImageBytes, Base64.DEFAULT);

        System.out.println(last4Digits + " " + nickName);

        Card card = new Card(nickName, coverImageEncoded, mappingKey, stegoObj.getHashMap_1(), last4Digits);

        addCardRequest(card);


    }

    public void addCardRequest(Card card) {

        StegoPayApi stegoPayApi = retrofit.create(StegoPayApi.class);

        String tokenFromSharedPrefs = getTokenFromSharedPrefs();

        Call<Card> call = stegoPayApi.createCard("Bearer " + tokenFromSharedPrefs, card);

        String userIDFromToken = getUserIDFromToken(tokenFromSharedPrefs);


        call.enqueue(new Callback<Card>() {
            @Override
            public void onResponse(Call<Card> call, Response<Card> response) {
                if (!response.isSuccessful()) {
                    System.out.println("HTTP Code:   " + response.code() + " " + response.message());
                    return;
                }

                Card cardResponse = response.body();

                db.addCard(userIDFromToken, cardResponse.getCardID(), card.getNickName(), card.getLast4Digits());

                progressBar.setVisibility(View.GONE);

                Intent i = new Intent(getApplicationContext(), ViewCards.class);
                startActivityForResult(i, VIEW_CARDS_REQUEST_CODE);


            }



            @Override
            public void onFailure(Call<Card> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println(t.getMessage());
            }
        });
    }

    //Converting a hashmap to string
    public String convertHashMapToString(HashMap toConvert){
        String converted = "";
        Iterator hmIterator = toConvert.entrySet().iterator();

        while(hmIterator.hasNext()){
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            converted += mapElement.getKey()+":"+mapElement.getValue()+";";
        }
        return converted;
    }

    public byte[] convertBitmapToByteArray() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        coverImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return byteArray;
    }

    // A method that gets the card details entered by the user
    public String getCardData() {
        String space = " ";

        // Get card details entered by the user
        String creditCardNumber = ccnEditText.getText().toString().replaceAll("\\s", "");
        String expDate = expDateEditText.getText().toString();
        String cvv = cvvEditText.getText().toString();

        // Storing details in a single string for mapping
        String ccDetails = creditCardNumber + space + expDate + space + cvv;

        return ccDetails;
    }

    // A method that converts a string to a binary string
    public String stringConvertToBinary (String input) {
        String binaryString = "";

        int length = input.length();

        for (int i = 0; i < length; i++) {
            //ASCII
            int val = Integer.valueOf(input.charAt(i));

            String temp = Integer.toBinaryString(val);

            temp = String.format("%8s", temp).replace(' ', '0');
            binaryString += temp;

        }
        return binaryString;
    }

    //Runnable to execute mapping operation
    private class mapping_background implements Runnable {

        private String ccDetailsBinary;

        //Default constructor to get encryted credit card details in binary
        mapping_background(String encryptedCCDetailsBinary) {
            this.ccDetailsBinary = encryptedCCDetailsBinary;
        }

        //Method to be executed in the background thread
        @Override
        public void run() {
            //to store the mapping key
            String mapping_key;

            //Start mapping, and get the mapping key
            mapping_key = stegoObj.single_pattern_mapping(ccDetailsBinary);

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

    // Preprocessing AsyncTask
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
            stegoObj.setImage(bitmaps[0]);

            //Start Preprocessing
            stegoObj.preProcessing2();

            //If the image selected is StegoPay ready
            if (stegoObj.check_image_validity()) {
                //Set coverImage to the selected image
                coverImage = bitmaps[0];

                //Set valid image selected boolean to true
                valid_image_selected = true;

                //Return true
                return true;
            } else {
                //Set image to null
                stegoObj.setImage(null);
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


            //If image selected was stegoPay ready
            if (aBoolean == true) {
                //Set TextView to selected image's name

                Cursor returnCursor =
                        getContentResolver().query(imageData, null, null, null, null);
                /*
                 * Get the column indexes of the data in the Cursor,
                 * move to the first row in the Cursor, get the data,
                 * and display it.
                 */
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();

                selectedImageNameTextView.setText(returnCursor.getString(nameIndex));


            } else {
                //Display a dialog to inform the user, and allow him to select another image
                new AlertDialog.Builder(AddCard.this)
                        .setTitle("Invalid Image")
                        .setMessage("The image selected is not StegoPay ready, try selecting an image with more colors.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectCoverImageButtonOnClick();
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


    //Receiving the image selected by the user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //to store image selected as a bitmap
            Bitmap temp = null;
            try {
                //Get image uri
                 imageData = data.getData();

                //Convert to bitmap
                InputStream imageStream = getContentResolver().openInputStream(imageData);
                temp = BitmapFactory.decodeStream(imageStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Execute AsyncTask for pre-processing the image
            new preprocessing().execute(temp);
        }
        if(requestCode == VIEW_CARDS_REQUEST_CODE && resultCode == RESULT_OK){
            finish();
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

}




