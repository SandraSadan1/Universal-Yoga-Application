package com.example.uya;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uya.model.YogaClass;
import com.example.uya.model.YogaCourse;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseListActivity extends AppCompatActivity {
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private static final String[] YOGA_CLASS_COLUMNS = {MyDatabaseHelper.ID, MyDatabaseHelper.COURSE_ID,
            MyDatabaseHelper.DAY, MyDatabaseHelper.COURSE_TIME, MyDatabaseHelper.TEACHER_NAME,
            MyDatabaseHelper.DATE, MyDatabaseHelper.COMMENTS};
    private static final String WEB_SERVICE_URL = "https://stuiis.cms.gre.ac.uk/COMP1424CoreWS/comp1424cw";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_list_layout);

        dbHelper = new MyDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        List<YogaCourse> data = fetchData();

        Button uploadButton = findViewById(R.id.btnupload);
        uploadButton.setOnClickListener(view -> uploadYogaData());


        TableLayout tableLayout = findViewById(R.id.tableLayout);

        for (YogaCourse course : data) {
            TableRow row = new TableRow(this);

            TextView dayTextView = new TextView(this);
            dayTextView.setText(course.getDay());
            row.addView(dayTextView);

            TextView timeTextView = new TextView(this);
            timeTextView.setText(course.getTimeOfCourse());
            row.addView(timeTextView);

            TextView durationTextView = new TextView(this);
            durationTextView.setText(course.getDuration());
            row.addView(durationTextView);

            TextView capacityTextView = new TextView(this);
            capacityTextView.setText(course.getCapacity());
            row.addView(capacityTextView);

            TextView priceTextView = new TextView(this);
            priceTextView.setText(String.valueOf(course.getPrice()));
            row.addView(priceTextView);

            TextView yogatypeTextView = new TextView(this);
            yogatypeTextView.setText(course.getYogaType());
            row.addView(yogatypeTextView);

            TextView descriptionTextView = new TextView(this);
            descriptionTextView.setText(course.getDescription());
            row.addView(descriptionTextView);

            // Add more TextViews for other columns...

            LinearLayout actionLayout = new LinearLayout(this);
            actionLayout.setOrientation(LinearLayout.HORIZONTAL);
            actionLayout.setGravity(Gravity.CENTER);

            ImageView editImageView = new ImageView(this);
            editImageView.setImageResource(R.drawable.edit);
            editImageView.setLayoutParams(new ViewGroup.LayoutParams(60, 60));
            actionLayout.addView(editImageView);

            ImageView deleteImageView = new ImageView(this);
            deleteImageView.setImageResource(R.drawable.delete);
            deleteImageView.setLayoutParams(new ViewGroup.LayoutParams(52, 52));
            actionLayout.addView(deleteImageView);

            // Get the course ID
            final int courseId = course.getId();

            // Set click listener for delete icon
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Perform the delete operation
                    deleteCourse(courseId);
                    // Optionally, you can refresh the UI or update the dataset after deletion
                    // For example, you can remove the row from the tableLayout
                    tableLayout.removeView(row);
                }
            });

            // Set click listener for edit icon
            editImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the ID or other relevant information from the clicked item
                    int courseId = course.getId();

                    Intent intent = new Intent(CourseListActivity.this, EditCourse.class);
                    // Pass the relevant information to the EditCourse activity
                    intent.putExtra("courseId", courseId);
                    startActivity(intent);
                }
            });

            row.addView(actionLayout);

            tableLayout.addView(row);
        }
    }

    public List<YogaCourse> fetchData() {
        List<YogaCourse> yogaCourses = new ArrayList<>();

        Cursor cursor = null;
        try {
            String query = "SELECT * FROM course_details ORDER BY " + MyDatabaseHelper.ID + " DESC";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Extract data from the cursor and create YogaCourse objects
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.ID));
                    String day = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DAY));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DURATION));
                    int price = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.PRICE));
                    String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COURSE_TIME));
                    String yogaType = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.YOGA_TYPE));
                    String capacity = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.CAPACITY));
                    String desc = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DESCRIPTION));

                    YogaCourse yogaCourse = new YogaCourse(id, day, duration, price, timeOfCourse, yogaType, capacity, desc);
                    yogaCourses.add(yogaCourse);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return yogaCourses;
    }

    public void deleteCourse(int courseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // Execute the DELETE statement
            db.delete("course_details", MyDatabaseHelper.ID + "=?", new String[]{String.valueOf(courseId)});
            showToast("Course deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    // Functions for upload all data to cloud

    public List<YogaClass> getAllYogaClasses() {
        List<YogaClass> yogaClasses = new ArrayList<>();
        Cursor cursor = db.query("yoga_class", YOGA_CLASS_COLUMNS, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.ID));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DATE));
                    int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COURSE_ID));
                    String day = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DAY));
                    String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COURSE_TIME));
                    String teacher = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.TEACHER_NAME));
                    String comments = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COMMENTS));
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

    public Map<Integer, List<YogaClass>> mapYogaClassesToCourses(List<YogaClass> yogaClasses, List<YogaCourse> yogaCourses) {
        Map<Integer, List<YogaClass>> mappedClasses = new HashMap<>();

        // Iterate through yoga classes and group them by courseId
        for (YogaClass yogaClass : yogaClasses) {
            int courseId = yogaClass.getCourseId();

            if (mappedClasses.containsKey(courseId)) {
                mappedClasses.get(courseId).add(yogaClass);
            } else {
                List<YogaClass> classList = new ArrayList<>();
                classList.add(yogaClass);
                mappedClasses.put(courseId, classList);
            }
        }

        // Iterate through yoga courses and update the class list
        for (YogaCourse yogaCourse : yogaCourses) {
            int courseId = yogaCourse.getId();
            if (mappedClasses.containsKey(courseId)) {
                yogaCourse.setClassList(mappedClasses.get(courseId));
            }
        }

        return mappedClasses;
    }

    public JSONObject generateCombinedJson() {
        List<YogaClass> yogaClasses = getAllYogaClasses();
        List<YogaCourse> yogaCourses = fetchData();

        Map<Integer, List<YogaClass>> groupedClasses = mapYogaClassesToCourses(yogaClasses, yogaCourses);

        // Create a JSON object to represent the combined data
        JSONObject combinedJson = new JSONObject();

        try {
            // Add userId field
            combinedJson.put("userId", "ss5282g");

            // Create an array to store detailList
            JSONArray detailListArray = new JSONArray();

            // Iterate through each yoga course and add its details to detailList
            for (YogaCourse yogaCourse : yogaCourses) {
                JSONObject courseObject = new JSONObject();

                courseObject.put("dayOfWeek", yogaCourse.getDay());
                courseObject.put("timeOfCourse", yogaCourse.getTimeOfCourse());
                courseObject.put("capacity", yogaCourse.getCapacity());
                courseObject.put("duration", yogaCourse.getDuration());
                courseObject.put("typeOfYoga", yogaCourse.getYogaType());
                courseObject.put("description", yogaCourse.getDescription());

                // Create an array to store classList
                JSONArray classListArray = new JSONArray();

                // Check if there are classes for the current course
                if (groupedClasses.containsKey(yogaCourse.getId())) {
                    List<YogaClass> classes = groupedClasses.get(yogaCourse.getId());

                    // Iterate through each class and add its details to classList
                    for (YogaClass yogaClass : classes) {
                        JSONObject classObject = new JSONObject();
                        classObject.put("date", yogaClass.getDate());
                        classObject.put("teacher", yogaClass.getTeacherName());
                        classObject.put("comments", yogaClass.getComments());
                        // Add other fields if needed
                        classListArray.put(classObject);
                    }
                }

                // Add classList to the courseObject
                courseObject.put("classList", classListArray);

                // Add other fields from YogaCourse if needed
                // courseObject.put("otherField", yogaCourse.getOtherField());

                // Add the courseObject to detailListArray
                detailListArray.put(courseObject);
            }

            // Add detailList to the combinedJson
            combinedJson.put("detailList", detailListArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return combinedJson;
    }

    public void uploadYogaData() {
        try {
            JSONObject payload = generateCombinedJson();
            // Convert data to JSON
            String jsonPayload = new Gson().toJson(payload);

            // To execute the AsyncTask
            new UploadTask().execute(jsonPayload);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UploadYogaCourses", "Error uploading yoga courses", e);
        }
    }

    private String sendPutRequest(String jsonPayload) throws Exception {
        URL url = new URL(WEB_SERVICE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return "Upload successful";
        } else {
            return "Upload failed: ";
        }
    }

    private class UploadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String jsonPayload = params[0];

            try {
                return sendPutRequest(jsonPayload);
            } catch (Exception e) {
                e.printStackTrace();
                return "Upload failed: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("UploadYogaCourses", result);
        }
    }

    public void navigateToHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class); // Replace HomeActivity with the name of your home screen activity
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        db.close();
        dbHelper.close(); // Close the database
        super.onDestroy();
    }
}
