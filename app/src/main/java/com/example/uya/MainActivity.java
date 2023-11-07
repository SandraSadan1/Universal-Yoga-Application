package com.example.uya;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.util.Log;
import com.example.uya.model.YogaCourse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText startTime;
    private EditText endTime;
    private EditText capacity;
    private EditText duration;
    private EditText pricePerClass;
    private Spinner dayOfWeek;
    private Spinner yogaType;
    private EditText description;
    private static final String ID = "id";
    private static final String DAY = "day";
    private static final String DURATION = "duration";
    private static final String CAPACITY = "capacity";
    private static final String PRICE = "price";
    private static final String START_TIME = "start_time";
    private static final String END_TIME = "end_time";
    private static final String YOGA_TYPE = "yoga_type";
    private static final String DESCRIPTION = "description";

    private Button submitButton;
    private static final String TAG = "Universal Yoga Application";
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database helper with the application context
        dbHelper = new MyDatabaseHelper(this);
        db = dbHelper.getWritableDatabase(); // Open the database for writing

        dayOfWeek = findViewById(R.id.dayOfWeek);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        capacity = findViewById(R.id.capacity);
        duration = findViewById(R.id.duration);
        pricePerClass = findViewById(R.id.pricePerClass);
        description = findViewById(R.id.description);
        yogaType = findViewById(R.id.typeOfClass);

        startTime.setOnClickListener(view -> showTimePickerDialog(startTime));
        endTime.setOnClickListener(view -> showTimePickerDialog(endTime));
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(view -> validateAndSubmitForm());
    }

    private void showTimePickerDialog(final EditText editText) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (timePicker, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    editText.setText(time);
                    editText.setError(null);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void clearAllEditTextFields() {
        EditText[] fields = {startTime, endTime, capacity, duration, pricePerClass, description};
        for (EditText field : fields) {
            field.setText("");
        }
    }

    private void validateAndSubmitForm() {
        boolean isValid = true;
        EditText[] fields = {startTime, endTime, capacity, duration, pricePerClass};

        for (EditText field : fields) {
            String fieldValue = field.getText().toString().trim();

            if (fieldValue.isEmpty()) {
                field.setError("This field is required");
                isValid = false;
            } else {
                field.setError(null);
            }
        }

        if (isValid) {
            ContentValues values = new ContentValues();
            values.put(DAY, dayOfWeek.getSelectedItem().toString());
            values.put(DURATION, duration.getText().toString());

            String priceValue = pricePerClass.getText().toString().trim();
            if (!priceValue.isEmpty()) {
                try {
                    int price = Integer.parseInt(priceValue);
                    values.put(PRICE, price);
                } catch (NumberFormatException e) {
                    pricePerClass.setError("Invalid price format");
                    isValid = false;
                }
            }

            values.put(START_TIME, startTime.getText().toString());
            values.put(END_TIME, endTime.getText().toString());
            values.put(YOGA_TYPE, yogaType.getSelectedItem().toString());
            values.put(CAPACITY, capacity.getText().toString());
            values.put(DESCRIPTION, description.getText().toString());

            if (isValid) {
                try {
                    long newRowId = db.insert("course_details", null, values);
                    if (newRowId != -1) {
                        clearAllEditTextFields(); // Clear fields after successful submission
                        Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
                        getAllYogaCourses();
                    } else {
                        Log.d(TAG, "Insertion failed");
                    }
                } catch (SQLException e) {
                    Log.e(TAG, "Database error: " + e.getMessage());
                    // Handle the error
                }
            }
        }
    }

    public List<YogaCourse> getAllYogaCourses() {
        List<YogaCourse> yogaCourses = new ArrayList<>();

        String[] columns = {ID, DAY, DURATION, PRICE, START_TIME, END_TIME, YOGA_TYPE, CAPACITY, DESCRIPTION};
        String selection = null;
        String[] selectionArgs = null;

        try {
            Cursor cursor = db.query("course_details", columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Retrieve data from the Cursor and create a YogaCourse object
                    int id = cursor.getInt(cursor.getColumnIndex(ID));
                    String day = cursor.getString(cursor.getColumnIndex(DAY));
                    String duration = cursor.getString(cursor.getColumnIndex(DURATION));
                    int price = cursor.getInt(cursor.getColumnIndex(PRICE));
                    String startTime = cursor.getString(cursor.getColumnIndex(START_TIME));
                    String endTime = cursor.getString(cursor.getColumnIndex(END_TIME));
                    String yogaType = cursor.getString(cursor.getColumnIndex(YOGA_TYPE));
                    String capacity = cursor.getString(cursor.getColumnIndex(CAPACITY));
                    String desc = cursor.getString(cursor.getColumnIndex(DESCRIPTION));

                    YogaCourse yogaCourse = new YogaCourse(id, day, duration, price, startTime, endTime, yogaType, capacity, desc);
                    yogaCourses.add(yogaCourse);
                    // Display the data in the log
                    Log.d("Data", "ID: " + id + ", Day: " + day + ", Duration: " + duration + ", Price: " + price + ", Start Time: " + startTime + ", End Time: " + endTime + ", Yoga Type: " + yogaType + ", Capacity: " + capacity + ", Description: " + description);
                } while (cursor.moveToNext());

                cursor.close();
            } else {
                Log.d(TAG, "Cursor is null or empty");
            }
        } catch (SQLException e) {
            Log.e(TAG, "Database error: " + e.getMessage());
            // Handle the error
        }

        return yogaCourses;
    }

    @Override
    protected void onDestroy() {
        db.close();
        dbHelper.close(); // Close the database
        super.onDestroy();
    }
}
