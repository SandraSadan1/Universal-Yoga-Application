package com.example.uya;

import static com.example.uya.MyDatabaseHelper.COMMENTS;
import static com.example.uya.MyDatabaseHelper.COURSE_ID;
import static com.example.uya.MyDatabaseHelper.COURSE_TIME;
import static com.example.uya.MyDatabaseHelper.DATE;
import static com.example.uya.MyDatabaseHelper.DAY;
import static com.example.uya.MyDatabaseHelper.ID;
import static com.example.uya.MyDatabaseHelper.TABLE_NAME;
import static com.example.uya.MyDatabaseHelper.TEACHER_NAME;
import static com.example.uya.MyDatabaseHelper.YOGA_TYPE;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uya.MyDatabaseHelper;
import com.example.uya.model.TimeSlot;
import com.example.uya.model.YogaClass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ClassInstances extends AppCompatActivity {

    private EditText dateEditText;
    private EditText dayEditText;
    private Spinner yogaType;
    private Spinner timeSelection;
    private Calendar currentDate;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private static final String TAG = "Universal Yoga Application";

    private Button submitButton;
    private TextView teacherTextView;
    private EditText teacherEditText;
    private Integer courseId;
    private TextView commentTextView;
    private EditText commentEditText;
    private String selectedTime;

    private static final String[] YOGA_CLASS_COLUMNS = {ID, COURSE_ID, DAY, COURSE_TIME, TEACHER_NAME, DATE, COMMENTS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_instances);
        dbHelper = new MyDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        dateEditText = findViewById(R.id.dateEditText);
        dayEditText = findViewById(R.id.dayEditText);
        yogaType = findViewById(R.id.typeOfClass);
        timeSelection = findViewById(R.id.timeEditView);
        submitButton = findViewById(R.id.submitButtonClass);
        teacherTextView = findViewById(R.id.teacherTextView);
        teacherEditText = findViewById(R.id.teacherEditText);
        commentTextView = findViewById(R.id.commentsTextView);
        commentEditText = findViewById(R.id.commentsEditText);

        currentDate = Calendar.getInstance();
    }

    private void setupListeners() {
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        yogaType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                fetchTimeSlotsElseSetDefaultValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                handleNothingSelected();
            }
        });

        timeSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                TimeSlot selectedTimeSlot = (TimeSlot) parentView.getItemAtPosition(position);
                dateEditText.setError(null);
                courseId = selectedTimeSlot.getId();
                selectedTime = selectedTimeSlot.getCourseTime();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case when nothing is selected
            }
        });

        submitButton.setOnClickListener(view -> validateAndSubmitForm());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                dateSetListener,
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
        currentDate.set(Calendar.YEAR, year);
        currentDate.set(Calendar.MONTH, month);
        currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateDateEditText();
    };

    private void updateDateEditText() {
        String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                currentDate.get(Calendar.DAY_OF_MONTH),
                currentDate.get(Calendar.MONTH) + 1,
                currentDate.get(Calendar.YEAR));
        dateEditText.setError(null);
        dateEditText.setText(selectedDate);
        String dayOfWeek = getDayOfWeekString(currentDate);
        dayEditText.setText(dayOfWeek);
        fetchTimeSlotsElseSetDefaultValue();
    }

    private String getDayOfWeekString(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    public List<TimeSlot> fetchTimeSlots() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        Cursor cursor = null;

        try {
            String selectedYogaType = yogaType.getSelectedItem().toString();
            String query = "SELECT " + ID + ", " + COURSE_TIME + " FROM " + TABLE_NAME +
                    " WHERE " + YOGA_TYPE + "=? AND " + DAY + "=?";
            String[] selectionArgs = {selectedYogaType, dayEditText.getText().toString()};

            cursor = db.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Extract ID and COURSE_TIME from the cursor
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
                    String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_TIME));
                    // Create a TimeSlot object and add it to the list
                    TimeSlot timeSlot = new TimeSlot(id, timeOfCourse);
                    timeSlots.add(timeSlot);
                    Log.d(TAG, "Time slot - ID: " + id + ", Time: " + timeOfCourse);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return timeSlots;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    private void fetchTimeSlotsElseSetDefaultValue() {
        List<TimeSlot> timeSlots = fetchTimeSlots();
        if (timeSlots.isEmpty()) {
            hideSelectedViews();
            setDefaultTimeSlots();
        } else {
            showSelectedViews();
            useProvidedTimeSlots(timeSlots);
        }
    }

    private void hideSelectedViews() {
        timeSelection.setVisibility(View.GONE);
        findViewById(R.id.timeView).setVisibility(View.GONE);
        teacherTextView.setVisibility(View.GONE);
        teacherEditText.setVisibility(View.GONE);
        commentTextView.setVisibility(View.GONE);
        commentEditText.setVisibility(View.GONE);
        teacherEditText.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
    }

    private void showSelectedViews() {
        timeSelection.setVisibility(View.VISIBLE);
        findViewById(R.id.timeView).setVisibility(View.VISIBLE);
        teacherTextView.setVisibility(View.VISIBLE);
        teacherEditText.setVisibility(View.VISIBLE);
        commentTextView.setVisibility(View.VISIBLE);
        commentEditText.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.VISIBLE);
    }

    private void setDefaultTimeSlots() {
        // Create a list with a default TimeSlot object
        List<TimeSlot> defaultTimeSlotsList = new ArrayList<>();
        defaultTimeSlotsList.add(new TimeSlot(-1, "No Slots Available"));

        // Create an ArrayAdapter with the list of TimeSlot objects
        ArrayAdapter<TimeSlot> defaultAdapter = new TimeSlotAdapter(this, defaultTimeSlotsList);
        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSelection.setAdapter(defaultAdapter);
    }

    private void useProvidedTimeSlots(List<TimeSlot> timeSlots) {
        TimeSlotAdapter adapter = new TimeSlotAdapter(this, timeSlots);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSelection.setAdapter(adapter);
    }

    private void handleNothingSelected() {
        Toast.makeText(getApplicationContext(), R.string.no_time_slots_available, Toast.LENGTH_SHORT).show();
    }

    public void navigateToHomeFromClass(View view) {
        Log.d(TAG, "Navigate to home");
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void validateAndSubmitForm() {
        boolean isValid = true;
        EditText[] fields = {dateEditText, teacherEditText};

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
            // Check if a teacher with the same yoga type, date and time already exists
            String date = dateEditText.getText().toString();
            if (classExists(date, courseId)) {
                Toast.makeText(this, "A teacher is already assigned for the selected date and time", Toast.LENGTH_SHORT).show();
                dateEditText.setError("A teacher is already assigned for the same yoga class");
            } else {
                ContentValues values = new ContentValues();
                values.put(DATE, date);
                values.put(DAY, dayEditText.getText().toString());
                values.put(COURSE_TIME, timeSelection.getSelectedItem().toString());
                values.put(TEACHER_NAME, teacherEditText.getText().toString());
                values.put(COURSE_ID, courseId);
                values.put(COMMENTS, commentEditText.getText().toString());

                if (isValid) {
                    try {
                        long newRowId = db.insert("yoga_class", null, values);
                        if (newRowId != -1) {
                            clearAllEditTextFields(); // Clear fields after successful submission
                            Toast.makeText(this, "Yoga class added successfully", Toast.LENGTH_SHORT).show();
                            getAllYogaClasses();
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
    }

    private boolean classExists(String date, Integer courseId) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM yoga_class WHERE " + DATE + " = ? AND " + COURSE_ID + " = ?", new String[]{date, String.valueOf(courseId)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    private void clearAllEditTextFields() {
        EditText[] fields = {dateEditText, dayEditText, teacherEditText};
        for (EditText field : fields) {
            field.setText("");
        }
        fetchTimeSlotsElseSetDefaultValue();
    }

    public List<YogaClass> getAllYogaClasses() {
        List<YogaClass> yogaClasses = new ArrayList<>();
        Cursor cursor = db.query("yoga_class", YOGA_CLASS_COLUMNS, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(DATE));
                    int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(COURSE_ID));
                    String day = cursor.getString(cursor.getColumnIndexOrThrow(DAY));
                    String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_TIME));
                    String teacher = cursor.getString(cursor.getColumnIndexOrThrow(TEACHER_NAME));
                    String comments = cursor.getString(cursor.getColumnIndexOrThrow(COMMENTS));
                    // Add the details to the list
                    YogaClass yogaClass = new YogaClass(id, date, courseId, day, timeOfCourse, teacher, comments);
                    yogaClasses.add(yogaClass);
                    // Display the data in the log
                    Log.d("Data", "ID: " + id + ", Day: " + day + ", teacher" + teacher + ", time" + timeOfCourse);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return yogaClasses;
    }

    @Override
    protected void onDestroy() {
        db.close();
        dbHelper.close(); // Close the database
        super.onDestroy();
    }
}
