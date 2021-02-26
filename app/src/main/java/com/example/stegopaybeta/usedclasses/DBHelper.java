package com.example.stegopaybeta.usedclasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    //Database name
    private String DATABASE_NAME = "stegoPayDB";


    public DBHelper(@Nullable Context context) {
        super(context, "stegoPayDB", null, 1);
    }

    //Called when the database is created for the first time
    //Create a users table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users(userId VARCHAR2 PRIMARY KEY, " +
                "firstName VARCHAR2, lastName VARCHAR2, email VARCHAR2, profileImage VARCHAR2 )";
        db.execSQL(createUsersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //Adding user to the users table
    public void addUser(String userId, String firstName, String lastName, String email, String profileImage) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("userId", userId);
        contentValues.put("firstName", firstName);
        contentValues.put("lastName", lastName);
        contentValues.put("email", email);
        contentValues.put("profileImage", profileImage);
        stegoPayDB.insert("users", null, contentValues);
    }

    //Get all users from the users table
    public Cursor getAllUsers() {
        SQLiteDatabase stegoPayDB = this.getReadableDatabase();
        Cursor allUsers = stegoPayDB.rawQuery("SELECT * FROM users", null);
        return allUsers;
    }

    //EDITED METHOD: Get a single user, pass the user id
    public Cursor getUser(String userId) {
        SQLiteDatabase stegoPayDB = this.getReadableDatabase();
        Cursor user = stegoPayDB.rawQuery("SELECT * FROM users WHERE userId= ?", new String[]{userId});
        return user;
    }

    //Update a users profile
    public void updateUser(String userId, String firstName, String lastName, String email, String profileImage) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("firstName", firstName);
        contentValues.put("lastName", lastName);
        contentValues.put("email", email);
        contentValues.put("profileImage", profileImage);
        stegoPayDB.update("users", contentValues, "userId=?", new String[]{userId});
    }

    //Create table for a users cards
    public void create_Users_Cards_Table(String userId) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();

        String create_Users_Cards_Table = "CREATE TABLE IF NOT EXISTS " + "user" + userId + "(cardId VARCHAR2 PRIMARY KEY, " +
                "nickName VARCHAR2, image VARCHAR2, hashMap_1 VARCHAR2, hashMap_2 VARCHAR2, last4Digits VARCHAR2 )";

        stegoPayDB.execSQL(create_Users_Cards_Table);
    }

    //Add card for a particular user
    public void addCard(String userId, String cardId, String nickName, String image, String hashMap_1, String hashMap_2, String last4Digits) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("cardId", cardId);
        contentValues.put("nickName", nickName);
        contentValues.put("image", image);
        contentValues.put("hashMap_1", hashMap_1);
        contentValues.put("hashMap_2", hashMap_2);
        contentValues.put("last4Digits", last4Digits);
        stegoPayDB.insert("user" + userId, null, contentValues);
    }

    //Get all cards of a user
    public Cursor getAllCards(String userId) {
        SQLiteDatabase stegoPayDB = this.getReadableDatabase();
        Cursor allCards = stegoPayDB.rawQuery("SELECT * FROM " + "user" + userId + "", null);
        return allCards;
    }

    //Get a single card
    public Cursor getCard(String userId, String cardId) {
        SQLiteDatabase stegoPayDB = this.getReadableDatabase();
        Cursor card = stegoPayDB.rawQuery("SELECT * FROM " + "user" + userId + " WHERE cardId=" + cardId + "", null);
        return card;
    }

    //Update Card
    public boolean updateCard(String userId, String cardId, String nickName, String image, String hashMap_1, String hashMap_2, String last4Digits) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("nickName", nickName);
        contentValues.put("image", image);
        contentValues.put("hashMap_1", hashMap_1);
        contentValues.put("hashMap_2", hashMap_2);
        contentValues.put("last4Digits", last4Digits);
        stegoPayDB.update("user" + userId, contentValues, "cardId=?", new String[]{cardId});
        return true;
    }

    //Delete card
    public boolean deleteCard(String userId, String cardId) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        stegoPayDB.delete("user" + userId, "cardId=?", new String[]{cardId});
        return true;
    }

    //NEW METHOD: Check if user already exists in the database based on their id
    public boolean checkUserId(String userId) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        Cursor user = stegoPayDB.rawQuery("SELECT * FROM users WHERE userId= ?", new String[]{userId});
        if (user.getCount() > 0) {
            return true;
        } else
            return false;

    }

    //NEW METHOD: Check if card already exists in the user's card table based on its id
    public boolean checkCardId(String userId, String cardId) {
        SQLiteDatabase stegoPayDB = this.getWritableDatabase();
        Cursor card = stegoPayDB.rawQuery("SELECT * FROM " + "user" + userId + " WHERE cardId= ?", new String[]{cardId});
        if (card.getCount() > 0) {
            return true;
        } else
            return false;

    }

}
