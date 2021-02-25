package com.example.stegopaybeta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String USER_PROFILE_TABLE = "USER_PROFILE_TABLE";
    public static final String COLUMN_USER_ID = "USER_ID";
    public static final String COLUMN_FIRST_NAME = "FIRST_NAME";
    public static final String COLUMN_LAST_NAME = "LAST_NAME";
    public static final String COLUMN_EMAIL = "EMAIL";


    public DataBaseHelper(@Nullable Context context) {
        super(context, "local.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    String createUserProfileTableStatement = "CREATE TABLE IF NOT EXISTS " + USER_PROFILE_TABLE + "(" + COLUMN_USER_ID + " TEXT PRIMARY KEY, " + COLUMN_FIRST_NAME + " TEXT, " + COLUMN_LAST_NAME + " TEXT, " + COLUMN_EMAIL + " TEXT)";
    db.execSQL(createUserProfileTableStatement);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public boolean addUserProfile(UserProfileModel userProfileModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER_ID, userProfileModel.getUserID());
        cv.put(COLUMN_FIRST_NAME, userProfileModel.getFirstName());
        cv.put(COLUMN_LAST_NAME, userProfileModel.getLastName());
        cv.put(COLUMN_EMAIL, userProfileModel.getEmail());

     long insert = db.insert(USER_PROFILE_TABLE, null, cv);

     if (insert == -1) {
         return false;
     } else {
         return true;
     }
    }

    public List<UserProfileModel> getAllUserProfiles() {
        List<UserProfileModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + USER_PROFILE_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {

            do {
                String userID = cursor.getString(0);
                String firstName = cursor.getString(1);
                String lastName = cursor.getString(2);
                String email = cursor.getString(3);

                UserProfileModel userProfile = new UserProfileModel(userID, firstName, lastName, email);
                returnList.add(userProfile);

            } while (cursor.moveToNext());

        } else {

        }

        cursor.close();
        db.close();
        return returnList;
    }


    public UserProfileModel getUserProfile(String USER_ID) {
        String queryString = "SELECT * FROM " + USER_PROFILE_TABLE + " WHERE USER_ID = "+ "'" + USER_ID + "'";

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            String userID = cursor.getString(0);
            String firstName = cursor.getString(1);
            String lastName = cursor.getString(2);
            String email = cursor.getString(3);

            UserProfileModel userProfile = new UserProfileModel(userID, firstName, lastName, email);
            return userProfile;
        }

        cursor.close();
        db.close();

        UserProfileModel userProfile = new UserProfileModel();
        return userProfile;
    }


    //Create table for a users cards
    public void create_Users_Cards_Table(String userId){
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();

        String create_Users_Cards_Table = "CREATE TABLE IF NOT EXISTS " + "user"+userId+ "(cardId VARCHAR2 PRIMARY KEY, " +
                "nickName VARCHAR2, image VARCHAR2, hashMap_1 VARCHAR2, hashMap_2 VARCHAR2, last4Digits VARCHAR2 )";

        stegoPayDB.execSQL(create_Users_Cards_Table);
    }

    public void dropTable(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS user" + userId);

        System.out.println("Done drop");

    }

    //Add card for a particular user
    public void addCard(String userId, String cardId, String nickName, String image, String hashMap_1, String hashMap_2, String last4Digits){
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("cardId",cardId);
        contentValues.put("nickName",nickName);
        contentValues.put("image",image);
        contentValues.put("hashMap_1",hashMap_1);
        contentValues.put("hashMap_2",hashMap_2);
        contentValues.put("last4Digits",last4Digits);
        stegoPayDB.insert("user"+userId,null, contentValues);
    }


    public ArrayList<Card> getAllCards(String userID) {

        ArrayList<Card> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM user" + userID;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {

            do {
                String cardID = cursor.getString(0);
                String nickName = cursor.getString(1);
                String image = cursor.getString(2);
                HashMap<Integer, String> hashMap1 = convertStringToHashMap_1(cursor.getString(3));
                HashMap<String, String> hashMap2 = convertStringToHashMap_2(cursor.getString(4));
                String last4Digits = cursor.getString(5);

                Card userCard = new Card(cardID, nickName, image, hashMap1, hashMap2, last4Digits);
                returnList.add(userCard);

            } while (cursor.moveToNext());

        } else {
        // Empty arraylist
        }

        cursor.close();
        db.close();
        return returnList;
    }

    //Converting string to hashmap 1
    //0:1X1;1:1X1;2:1X1;3:1X1;4:1X1;5:1X1;6:1X1;7:1X1;
    public HashMap<Integer,String> convertStringToHashMap_1(String toConvert){
        HashMap<Integer,String> hashMap_1 = new HashMap<Integer, String>();

        String[] singleEntry = toConvert.split(";");
        for(int i=0;i<singleEntry.length;i++){
            String[] keyValue = singleEntry[i].split(":");
            hashMap_1.put(Integer.parseInt(keyValue[0]),keyValue[1]);
        }


        return hashMap_1;

    }
    //Converting string to hashmap2
    //1X1:0000000000;2X1:1110010101
    public HashMap<String,String> convertStringToHashMap_2(String toConvert){
        HashMap<String,String> hashMap_2 = new HashMap<String, String>();

        String[] singleEntry = toConvert.split(";");
        for(int i=0;i<singleEntry.length;i++){
            String[] keyValue = singleEntry[i].split(":");
            hashMap_2.put(keyValue[0],keyValue[1]);
        }

        return hashMap_2;
    }





}
