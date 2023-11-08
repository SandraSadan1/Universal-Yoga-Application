package com.example.uya;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UniversalYogaApp";
    private static final int DATABASE_VERSION = 13;

    // Table and column names
    public static final String TABLE_NAME = "course_details";
    public static final String ID = "id";
    public static final String DAY = "day";
    public static final String DURATION = "duration";
    public static final String CAPACITY = "capacity";
    public static final String PRICE = "price";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String YOGA_TYPE = "yoga_type";
    public static final String DESCRIPTION = "description";

    // Define an array of column names for easy access
    public static final String[] COLUMNS = {
            ID, DAY, DURATION, CAPACITY, PRICE, START_TIME, END_TIME, YOGA_TYPE, DESCRIPTION
    };

    // Define SQL statements as constants
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DAY + " TEXT, " +
                    DURATION + " TEXT, " +
                    PRICE + " INTEGER, " +
                    START_TIME + " TEXT, " +
                    END_TIME + " TEXT, " +
                    YOGA_TYPE + " TEXT, " +
                    CAPACITY + " TEXT, " +
                    DESCRIPTION + " TEXT" +
                    ")";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("MyDatabaseHelper", "onCreate method is executed");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // Drop the old table (if it exists)
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            // Recreate the table with the new schema
            onCreate(db);
        }
    }
}
