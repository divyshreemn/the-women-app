package com.example.womensafetyapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "relatives.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_PARENT_NAME = "parent_name";
    public static final String COLUMN_NUMBER = "number";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_PARENT_NAME + " TEXT," +
                    COLUMN_NUMBER + " TEXT" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public List<String> getAllPhoneNumbers() {
        List<String> phoneNumbers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_NUMBER}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String phoneNumber = cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER));
            phoneNumbers.add(phoneNumber);
        }
        cursor.close();
        return phoneNumbers;
    }
}