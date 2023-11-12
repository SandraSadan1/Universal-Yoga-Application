package com.example.uya;

import static com.example.uya.MyDatabaseHelper.COURSE_TIME;
import static com.example.uya.MyDatabaseHelper.DAY;
import static com.example.uya.MyDatabaseHelper.YOGA_TYPE;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
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
    private static final String TAG = "Universal Yoga Application";

    private Button submitButton;
    private TextView teacherTextView;
    private EditText teacherEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_instances);
        dbHelper = new MyDatabaseHelper(this);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        dateEditText = findViewById(R.id.dateEditText);
        dayEditText = findViewById(R.id.dayEditText);
        yogaType = findViewById(R.id.typeOfClass);
        timeSelection = findViewById(R.id.timeEditView);
        submitButton = findViewById(R.id.submitButton);
        teacherTextView = findViewById(R.id.teacherTextView);
        teacherEditText = findViewById(R.id.teacherEditText);

        if (!dateEditText.getText().toString().isEmpty()) {
            timeSelection.setVisibility(View.VISIBLE);
            findViewById(R.id.timeView).setVisibility(View.VISIBLE);
        }
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
        dateEditText.setText(selectedDate);
        String dayOfWeek = getDayOfWeekString(currentDate);
        dayEditText.setText(dayOfWeek);
        timeSelection.setVisibility(View.VISIBLE);
        findViewById(R.id.timeView).setVisibility(View.VISIBLE);
        fetchTimeSlotsElseSetDefaultValue();
    }

    private String getDayOfWeekString(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    public List<String> fetchTimeSlots() {
        List<String> timeSlots = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String selectedYogaType = yogaType.getSelectedItem().toString();
            String query = "SELECT DISTINCT " + COURSE_TIME + " FROM course_details WHERE " + YOGA_TYPE + "=? AND " + DAY + "=?";
            String[] selectionArgs = {selectedYogaType, dayEditText.getText().toString()};
            Log.d(TAG, "executing Time slots" + selectedYogaType + "day" + dayEditText.getText());

            cursor = db.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_TIME));
                    timeSlots.add(timeOfCourse);
                    Log.d(TAG, "Time slots" + timeOfCourse);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursorAndDatabase(cursor, db);
        }

        return timeSlots;
    }

    private void closeCursorAndDatabase(Cursor cursor, SQLiteDatabase db) {
        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }

    private void fetchTimeSlotsElseSetDefaultValue() {
        List<String> timeSlots = fetchTimeSlots();
        if (timeSlots.isEmpty()) {
            hideTeacherViews();
            setDefaultTimeSlots();
        } else {
            showTeacherViews();
            useProvidedTimeSlots(timeSlots);
        }
    }

    private void hideTeacherViews() {
        teacherTextView.setVisibility(View.GONE);
        teacherEditText.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
    }

    private void showTeacherViews() {
        teacherTextView.setVisibility(View.VISIBLE);
        teacherEditText.setVisibility(View.VISIBLE);
        submitButton.setVisibility(View.VISIBLE);
    }

    private void setDefaultTimeSlots() {
        String[] defaultTimeSlots = {"No Slots Available"};
        ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defaultTimeSlots);
        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSelection.setAdapter(defaultAdapter);
    }

    private void useProvidedTimeSlots(List<String> timeSlots) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeSlots);
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
}
