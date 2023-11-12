package com.example.uya;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "UniversalYogaApp";
    public static final int DATABASE_VERSION = 15;
    // Table and column names
    public static final String TABLE_NAME = "course_details";
    public static final String ID = "id";
    public static final String DAY = "day";
    public static final String DURATION = "duration";
    public static final String CAPACITY = "capacity";
    public static final String PRICE = "price";
    public static final String COURSE_TIME = "course_time";
    public static final String YOGA_TYPE = "yoga_type";
    public static final String DESCRIPTION = "description";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DAY + " TEXT, " +
                    DURATION + " TEXT, " +
                    PRICE + " INTEGER, " +
                    COURSE_TIME + " TEXT, " +
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