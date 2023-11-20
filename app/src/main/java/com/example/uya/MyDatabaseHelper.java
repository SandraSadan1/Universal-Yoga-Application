package com.example.uya;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.uya.model.YogaClass;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String TAG = "MyDatabaseHelper";
    public static final String DATABASE_NAME = "UniversalYogaApp";
    public static final int DATABASE_VERSION = 21;
    // Course details table and column names
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
    // Yoga class table and column names
    public static final String TABLE_YOGA_CLASS = "yoga_class";
    public static final String COURSE_ID = "course_id";
    public static final String DATE = "course_date";
    public static final String TEACHER_NAME = "teacher_name";
    public static final String COMMENTS = "comments";
    private static final String CREATE_YOGA_CLASS_TABLE =
            "CREATE TABLE " + TABLE_YOGA_CLASS + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COURSE_ID + " INTEGER, " +
                    DAY + " TEXT, " +
                    COURSE_TIME + " TEXT, " +
                    DATE + " TEXT, " +
                    TEACHER_NAME + " TEXT, " +
                    COMMENTS + " TEXT" +
                    ")";
    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("MyDatabaseHelper", "onCreate method is executed");
        db.execSQL(CREATE_TABLE); // for course details
        db.execSQL(CREATE_YOGA_CLASS_TABLE); // for yoga class
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // Drop the old table (if it exists)
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASS);
            // Recreate the table with the new schema
            onCreate(db);
        }
    }

    public List<YogaClass> queryClassesForDay(String selectedDay) {
        List<YogaClass> yogaClasses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the columns you want to retrieve
        String[] projection = {
                ID,
                COURSE_ID,
                DAY,
                COURSE_TIME,
                DATE,
                TEACHER_NAME,
                COMMENTS
        };

        // Specify the selection criteria
        String selection = "LOWER(" + DAY + ") = LOWER(?)";
        String[] selectionArgs = {selectedDay.toLowerCase()};

        // Query the database
        Cursor cursor = db.query(
                TABLE_YOGA_CLASS,    // Table to query
                projection,          // Columns to return
                selection,           // Columns for the WHERE clause
                selectionArgs,       // Values for the WHERE clause
                null,                // Don't group the rows
                null,                // Don't filter by row groups
                null                 // The sort order
        );

        // Iterate through the result set and create YogaClass objects
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
            int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(COURSE_ID));
            String day = cursor.getString(cursor.getColumnIndexOrThrow(DAY));
            String courseTime = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_TIME));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DATE));
            String teacherName = cursor.getString(cursor.getColumnIndexOrThrow(TEACHER_NAME));
            String comments = cursor.getString(cursor.getColumnIndexOrThrow(COMMENTS));

            YogaClass yogaClass = new YogaClass(id, date, courseId, day, courseTime, teacherName, comments);
            yogaClasses.add(yogaClass);
        }
// Close the cursor and database
        cursor.close();
        db.close();

        return yogaClasses;
    }
}