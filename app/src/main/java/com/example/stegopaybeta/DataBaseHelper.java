package com.example.stegopaybeta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.util.Base64;

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
    public static final String COLUMN_PROFILE_IMAGE = "PROFILE_IMAGE";



    public DataBaseHelper(@Nullable Context context) {
        super(context, "local.db", null, 1); }

    @Override
    public void onCreate(SQLiteDatabase db) {
    String createUserProfileTableStatement = "CREATE TABLE IF NOT EXISTS " + USER_PROFILE_TABLE + "(" + COLUMN_USER_ID + " TEXT PRIMARY KEY, " + COLUMN_FIRST_NAME + " TEXT, " + COLUMN_LAST_NAME + " TEXT, " + COLUMN_EMAIL + " TEXT," + COLUMN_PROFILE_IMAGE + " TEXT)";
    db.execSQL(createUserProfileTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    //Create table for a users cards
    public void create_Users_Cards_Table(String userId){
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();

        String create_Users_Cards_Table = "CREATE TABLE IF NOT EXISTS " + "user"+userId+ "(cardId VARCHAR2 PRIMARY KEY, " +
                "nickName VARCHAR2, last4Digits VARCHAR2 )";

        stegoPayDB.execSQL(create_Users_Cards_Table);
    }

    public void dropCardsTable(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS user" + userId);

        System.out.println("Done drop");

    }

    public void dropUserTable() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + USER_PROFILE_TABLE);

        System.out.println("Done drop");

    }

    //Add card for a particular user
    public void addCard(String userId, String cardId, String nickName, String last4Digits){
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("cardId",cardId);
        contentValues.put("nickName",nickName);
        contentValues.put("last4Digits",last4Digits);
        stegoPayDB.insert("user"+userId,null, contentValues);
    }

    //Update Card
    public boolean updateCard(String userId,String cardId, String nickName, String last4Digits){
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("nickName",nickName);
        contentValues.put("last4Digits",last4Digits);
        stegoPayDB.update("user"+userId,contentValues,"cardId=?",new String[]{cardId});
        return true;
    }

    //Delete card
    public boolean deleteCard(String userId, String cardId){
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        stegoPayDB.delete("user"+userId,"cardId=?",new String[]{cardId});
        return true;
    }

    public ArrayList<Card> getAllCards(String userID) {

        ArrayList<Card> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM user" + userID;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);


        if (cursor.moveToFirst()) {
            do {
                String cardID = cursor.getString(0);
                System.out.println("CARD: "+cardID);
                String nickName = cursor.getString(1);
                String last4Digits = cursor.getString(2);

                Card userCard = new Card(cardID, nickName, last4Digits);
                returnList.add(userCard);

            } while (cursor.moveToNext());

        } else {
        // Empty arraylist
        }

        cursor.close();
        db.close();
        return returnList;
    }

    // Check if user already exists in the database based on their id
    public boolean checkUserId(String userId) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        Cursor user = stegoPayDB.rawQuery("SELECT * FROM " + USER_PROFILE_TABLE + " WHERE " + COLUMN_USER_ID + "= ?", new String[]{userId});
        if (user.getCount() > 0) {
            return true;
        } else
            return false;
    }

    // Check if card already exists in the user's card table based on its id
    public boolean checkCardId(String userId, String cardId) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        Cursor card = stegoPayDB.rawQuery("SELECT * FROM " + "user" + userId + " WHERE cardId= ?", new String[]{cardId});
        if (card.getCount() > 0) {
            return true;
        } else
            return false;

    }

    //Adding user to the users table
    public void addUser(String userId, String firstName, String lastName, String email, String profileImage) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USER_ID, userId);
        contentValues.put(COLUMN_FIRST_NAME, firstName);
        contentValues.put(COLUMN_LAST_NAME, lastName);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_PROFILE_IMAGE, profileImage);
        stegoPayDB.insert(USER_PROFILE_TABLE, null, contentValues);
    }

    // Get a single user, pass the user id
    public Cursor getUser (String userId) {
        SQLiteDatabase stegoPayDB = this.getReadableDatabase();
        Cursor cursor = stegoPayDB.rawQuery("SELECT * FROM " + USER_PROFILE_TABLE + " WHERE " + COLUMN_USER_ID + "= ?", new String[]{userId});
        return cursor;
    }

    public String getUserFirstName (String userId) {
        SQLiteDatabase stegoPayDB = this.getReadableDatabase();

        String queryString = "SELECT * FROM " + USER_PROFILE_TABLE + " WHERE " + COLUMN_USER_ID + " = " + "'" + userId + "'";

        Cursor cursor = stegoPayDB.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            String firstName = cursor.getString(1);
            return firstName;
        }

        cursor.close();
        stegoPayDB.close();

        return "N/A";
    }


    public String getUserProfileImage (String userId) {
        SQLiteDatabase stegoPayDB = this.getReadableDatabase();

        String queryString = "SELECT * FROM " + USER_PROFILE_TABLE + " WHERE " + COLUMN_USER_ID + " = " + "'" + userId + "'";

        Cursor cursor = stegoPayDB.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            String profileImage = cursor.getString(4);
            return profileImage;
        }

        cursor.close();
        stegoPayDB.close();

        return "N/A";
    }



    //Update a users profile
    public void updateUser(String userId, String firstName, String lastName, String email, String profileImage) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FIRST_NAME, firstName);
        contentValues.put(COLUMN_LAST_NAME, lastName);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_PROFILE_IMAGE, profileImage);
        stegoPayDB.update(USER_PROFILE_TABLE, contentValues, COLUMN_USER_ID +"=?", new String[]{userId});
    }



}
