package com.example.uya;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uya.model.YogaCourse;

import java.util.ArrayList;
import java.util.List;

public class EditCourse extends AppCompatActivity {

    private EditText timeOfCourse;
    private EditText capacity;
    private EditText duration;
    private EditText pricePerClass;
    private Spinner dayOfWeek;
    private Spinner yogaType;
    private EditText description;
    private int courseId;
    private MyDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        // Retrieve the information passed from the previous activity
        courseId = getIntent().getIntExtra("courseId", -1);

        // Initialize databaseHelper
        databaseHelper = new MyDatabaseHelper(this);

        // Initialize views
        dayOfWeek = findViewById(R.id.dayOfWeek);
        timeOfCourse = findViewById(R.id.timeOfCourse);
        capacity = findViewById(R.id.capacity);
        duration = findViewById(R.id.duration);
        pricePerClass = findViewById(R.id.pricePerClass);
        description = findViewById(R.id.description);
        yogaType = findViewById(R.id.typeOfClass);

        // Disable the Spinners
        dayOfWeek.setEnabled(false);
        yogaType.setEnabled(false);

        // Fetch data from the database based on courseId
        fetchCourseData(courseId);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(view -> validateAndSubmitForm());
    }

    private void fetchCourseData(int courseId) {
        // Create an instance of CourseList
        CourseList courseList = new CourseList(this);

        // Fetch the course by ID
        YogaCourse course = courseList.fetchCourseById(courseId);

        // Now you have the course data, you can use it as needed
        if (course != null) {
            // Set values in the form fields
            timeOfCourse.setText(course.getTimeOfCourse());
            capacity.setText(course.getCapacity());
            duration.setText(course.getDuration());
            pricePerClass.setText(String.valueOf(course.getPrice()));
            description.setText(course.getDescription());
        } else {
            // Handle the case where the course data is not found
            Toast.makeText(this, "Course data not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndSubmitForm() {
        boolean isValid = true;
        EditText[] fields = {timeOfCourse, capacity, duration, pricePerClass};

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
            values.put(MyDatabaseHelper.DURATION, duration.getText().toString());
            String priceValue = pricePerClass.getText().toString().trim();

            if (!priceValue.isEmpty()) {
                try {
                    int price = Integer.parseInt(priceValue);
                    values.put(MyDatabaseHelper.PRICE, price);
                } catch (NumberFormatException e) {
                    pricePerClass.setError("Invalid price format");
                    isValid = false;
                }
            }

            values.put(MyDatabaseHelper.CAPACITY, capacity.getText().toString());
            values.put(MyDatabaseHelper.DESCRIPTION, description.getText().toString());

            if (isValid) {
                try {
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    long idToUpdate = courseId;
                    int rowsAffected = db.update("course_details", values, "id=?", new String[]{String.valueOf(idToUpdate)});

                    if (rowsAffected > 0) {
                        Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                        getAllYogaCourses();
                        navigateToList();
                    } else {
                        Log.d(MyDatabaseHelper.TAG, "Update failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(MyDatabaseHelper.TAG, "Error updating course: " + e.getMessage());
                }
            }
        }
    }

    public List<YogaCourse> getAllYogaCourses() {
        List<YogaCourse> yogaCourses = new ArrayList<>();

        String[] columns = {MyDatabaseHelper.COURSE_ID, MyDatabaseHelper.DAY, MyDatabaseHelper.DURATION,
                MyDatabaseHelper.PRICE, MyDatabaseHelper.COURSE_TIME, MyDatabaseHelper.YOGA_TYPE,
                MyDatabaseHelper.CAPACITY, MyDatabaseHelper.DESCRIPTION};
        String selection = null;
        String[] selectionArgs = null;

        try {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            Cursor cursor = db.query("course_details", columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COURSE_ID));
                    String day = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DAY));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DURATION));
                    int price = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.PRICE));
                    String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COURSE_TIME));
                    String yogaType = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.YOGA_TYPE));
                    String capacity = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.CAPACITY));
                    String desc = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DESCRIPTION));

                    YogaCourse yogaCourse = new YogaCourse(id, day, duration, price, timeOfCourse, yogaType, capacity, desc);
                    yogaCourses.add(yogaCourse);
                    Log.d("Data", "ID: " + id + ", Day: " + day + ", Duration: " + duration + ", Price: " + price +
                            ", Course Time: " + timeOfCourse + ", Yoga Type: " + yogaType + ", Capacity: " + capacity + ", Description: " + desc);
                } while (cursor.moveToNext());

                cursor.close();
            } else {
                Log.d(MyDatabaseHelper.TAG, "Cursor is null or empty");
            }
        } catch (SQLException e) {
            Log.e(MyDatabaseHelper.TAG, "Database error: " + e.getMessage());
        }

        return yogaCourses;
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    public void navigateToHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
    public void navigateToList() {
        Intent intent = new Intent(this, CourseListActivity.class);
        startActivity(intent);
    }
    public class CourseList {
        private Context context;

        public CourseList(Context context) {
            this.context = context;
            databaseHelper = new MyDatabaseHelper(context);
        }

        public YogaCourse fetchCourseById(int courseId) {
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String query = "SELECT * FROM course_details WHERE id = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(courseId)});

            YogaCourse fetchedCourse = null;

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String day = cursor.getString(cursor.getColumnIndexOrThrow("day"));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"));
                    int price = cursor.getInt(cursor.getColumnIndexOrThrow("price"));
                    String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow("course_time"));
                    String yogaType = cursor.getString(cursor.getColumnIndexOrThrow("yoga_type"));
                    String capacity = cursor.getString(cursor.getColumnIndexOrThrow("capacity"));
                    String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                    fetchedCourse = new YogaCourse(id, day, duration, price, timeOfCourse, yogaType, capacity, desc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                db.close();
            }

            return fetchedCourse;
        }
    }
}
