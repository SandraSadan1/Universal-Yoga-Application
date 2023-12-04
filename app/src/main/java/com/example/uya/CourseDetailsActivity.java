package com.example.uya;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uya.model.YogaClass;
import com.example.uya.model.YogaCourse;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CourseDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourseDetailsAdapter adapter;
    private List<YogaCourse> yogaCourses;
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    // Views for the CardView
    private CardView cardViewItem;
    private TextView textViewDay, texttime, textduration, textprice, textcapacity, texttype, textdescription;
    private Button uploadButton;
    private static final String[] YOGA_CLASS_COLUMNS = {MyDatabaseHelper.ID, MyDatabaseHelper.COURSE_ID,
            MyDatabaseHelper.DAY, MyDatabaseHelper.COURSE_TIME, MyDatabaseHelper.TEACHER_NAME,
            MyDatabaseHelper.DATE, MyDatabaseHelper.COMMENTS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        dbHelper = new MyDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // Views for the CardView
        cardViewItem = findViewById(R.id.cardViewItem);
        textViewDay = findViewById(R.id.textViewDay);
        texttime = findViewById(R.id.texttime);
        textduration = findViewById(R.id.textduration);
        textprice = findViewById(R.id.textprice);
        textcapacity = findViewById(R.id.textcapacity);
        texttype = findViewById(R.id.texttype);
        textdescription = findViewById(R.id.textdescription);

        // Initially, set the visibility of cardViewItem to GONE
        cardViewItem.setVisibility(View.GONE);

        yogaCourses = getCourseDetails();
        populatelist();
        // Show the CardView with details for the first item when the adapter is bound
        if (!yogaCourses.isEmpty()) {
            showCourseDetails(yogaCourses.get(0));
        }
        // Set up Home Icon Click
        ImageView imageViewHome = findViewById(R.id.homeButton);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateHome(v);
            }
        });

        uploadButton = findViewById(R.id.btnupload);
        uploadButton.setOnClickListener(view -> uploadYogaData());
    }


    private void populatelist(){
        List<YogaCourse> data = getCourseDetails();
        if (data.isEmpty()) {
            //uploadButton.setVisibility(View.GONE);
            TableRow noDataRow = new TableRow(this);
            TextView noDataTextView = new TextView(this);
            noDataTextView.setText("No Data Available");
            noDataTextView.setGravity(Gravity.CENTER);
            noDataTextView.setPaddingRelative(400,100,10,50);
            noDataTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            noDataTextView.setTextColor(ContextCompat.getColor(this, R.color.redColor)); // Adjust the color as needed

            noDataRow.addView(noDataTextView);

            // Add the noDataRow to the layout
            ((ViewGroup) recyclerView.getParent()).addView(noDataRow);

            // Hide the recyclerView
            recyclerView.setVisibility(View.GONE);
        } else {
            // Show the recyclerView
            recyclerView.setVisibility(View.VISIBLE);

            // Set up the adapter when data is available
            adapter = new CourseDetailsAdapter(yogaCourses);
            recyclerView.setAdapter(adapter);
        }
    }

    private void showCourseDetails(YogaCourse yogaCourse) {
            cardViewItem.setVisibility(View.VISIBLE);
            textViewDay.setText("Day: " + yogaCourse.getDay());
            texttime.setText("Time: " + yogaCourse.getTimeOfCourse());
            textduration.setText("Duration: " + yogaCourse.getDuration());
            textprice.setText("Price(£): " + String.valueOf(yogaCourse.getPrice()));
            textcapacity.setText("Capacity: " + yogaCourse.getCapacity());
            texttype.setText("Yoga Type: " + yogaCourse.getYogaType());
            textdescription.setText("Description: " + yogaCourse.getDescription());

    }

    private List<YogaCourse> getCourseDetails() {
        List<YogaCourse> yogaCourses = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                MyDatabaseHelper.ID,
                MyDatabaseHelper.DAY,
                MyDatabaseHelper.DURATION,
                MyDatabaseHelper.PRICE,
                MyDatabaseHelper.COURSE_TIME,
                MyDatabaseHelper.YOGA_TYPE,
                MyDatabaseHelper.CAPACITY,
                MyDatabaseHelper.DESCRIPTION
        };

        Cursor cursor = db.query(
                MyDatabaseHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.ID));
            String day = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DAY));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DURATION));
            int price = cursor.getInt(cursor.getColumnIndexOrThrow(MyDatabaseHelper.PRICE));
            String courseTime = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COURSE_TIME));
            String yogaType = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.YOGA_TYPE));
            String capacity = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.CAPACITY));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.DESCRIPTION));

            YogaCourse yogaCourse = new YogaCourse(id, day, duration, price, courseTime, yogaType, capacity, description);
            yogaCourses.add(yogaCourse);
        }

        cursor.close();

        return yogaCourses;
    }
    private void deleteCourse(int courseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            // Delete associated classes first
            db.delete(MyDatabaseHelper.TABLE_YOGA_CLASS, MyDatabaseHelper.COURSE_ID + "=?", new String[]{String.valueOf(courseId)});

            // Then, delete the course
            db.delete(MyDatabaseHelper.TABLE_NAME, MyDatabaseHelper.ID + "=?", new String[]{String.valueOf(courseId)});
            showToast("Course and associated classes deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }



    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class CourseDetailsAdapter extends RecyclerView.Adapter<CourseDetailsAdapter.ViewHolder> {

        private List<YogaCourse> yogaCourses;

        public CourseDetailsAdapter(List<YogaCourse> yogaCourses) {
            this.yogaCourses = yogaCourses;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_course_details_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            YogaCourse yogaCourse = yogaCourses.get(position);

            holder.itemView.setOnClickListener(v -> {
                // Show the CardView with details when an item is clicked
                showCourseDetails(yogaCourse);
            });

            // Bind data to the item views
            holder.bind(yogaCourse);
        }

        @Override
        public int getItemCount() {
            return yogaCourses.size();
        }

        public void updateData(List<YogaCourse> newData) {
            yogaCourses = newData;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            public void bind(YogaCourse yogaCourse) {
                TextView textViewDayItem = itemView.findViewById(R.id.textViewDayItem);
                TextView textViewTimeItem = itemView.findViewById(R.id.textViewTimeItem);
                TextView textViewDurationItem = itemView.findViewById(R.id.textViewDurationItem);
                TextView textViewPriceItem = itemView.findViewById(R.id.textViewPriceItem);
                TextView textViewCapacityItem = itemView.findViewById(R.id.textViewCapacityItem);
                TextView textViewTypeItem = itemView.findViewById(R.id.textViewTypeItem);
                TextView textViewDescriptionItem = itemView.findViewById(R.id.textViewDescriptionItem);

                textViewDayItem.setText("Day        : " + yogaCourse.getDay());
                textViewTimeItem.setText("Time       : " + yogaCourse.getTimeOfCourse());
                textViewDurationItem.setText("Duration   : " + yogaCourse.getDuration() + " minutes");
                textViewPriceItem.setText("Price(£)   : " + yogaCourse.getPrice());
                textViewCapacityItem.setText("Capacity   : " + yogaCourse.getCapacity());
                textViewTypeItem.setText("Yoga Type  : " + yogaCourse.getYogaType());
                textViewDescriptionItem.setText(TextUtils.isEmpty(yogaCourse.getDescription()) ? "Description:----" : "Description:"+ yogaCourse.getDescription());


                // Edit Icon
                ImageView editImageView = itemView.findViewById(R.id.imageViewEdit);
                editImageView.setOnClickListener(v -> {
                    // Get the ID or other relevant information from the clicked item
                    int courseId = yogaCourse.getId();

                    // Create an intent to navigate to EditCourse activity
                    Intent intent = new Intent(itemView.getContext(), EditCourse.class);

                    // Pass the relevant information to the EditCourse activity
                    intent.putExtra("courseId", courseId);

                    // Start the EditCourse activity
                    itemView.getContext().startActivity(intent);
                });

                // Delete Icon
                ImageView deleteImageView = itemView.findViewById(R.id.imageViewDelete);
                deleteImageView.setOnClickListener(v -> {
                    // Create and show an AlertDialog to confirm deletion
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to delete this course and its associated classes?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // User clicked "Yes", perform the delete operation
                                deleteCourse(yogaCourse.getId());
                                updateData(getCourseDetails());
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                // User clicked "No," do nothing, or you can add additional logic
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                });
            }
        }
    }

    // Handle Home Icon Click
    public void navigateHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

//    Functions for upload course

    public void uploadYogaData() {
        try {
            Map<String, Object> payload = generateCombinedJson();
            Gson gson = new Gson();
            String jsonpayload = "jsonpayload=" + URLEncoder.encode(gson.toJson(payload), "UTF-8");

            // To execute the AsyncTask
            new CourseDetailsActivity.UploadTask().execute(jsonpayload);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UploadYogaCourses", "Error uploading yoga courses", e);
        }
    }

    public Map<String, Object> generateCombinedJson() {
        List<YogaClass> yogaClasses = getAllYogaClasses();
        List<YogaCourse> yogaCourses = getCourseDetails();

        Map<Integer, List<YogaClass>> groupedClasses = mapYogaClassesToCourses(yogaClasses, yogaCourses);

        // Create a JSON object to represent the combined data
        Map<String, Object> combinedJson = new HashMap<>();

        try {
            // Add userId field
            combinedJson.put("userId", "mm5290q");

            // Create an array to store detailList
            List<Map<String, Object>> detailListArray = new ArrayList<>();

            // Iterate through each yoga course and add its details to detailList
            for (YogaCourse yogaCourse : yogaCourses) {
                Map<String, Object> courseObject = new HashMap<>();

                courseObject.put("dayOfWeek", yogaCourse.getDay());
                courseObject.put("timeOfDay", yogaCourse.getTimeOfCourse());
                courseObject.put("capacity", yogaCourse.getCapacity());
                courseObject.put("duration", yogaCourse.getDuration());
                courseObject.put("typeOfYoga", yogaCourse.getYogaType());
                courseObject.put("description", yogaCourse.getDescription());

                // Create an array to store classList
                List<Map<String, Object>> classListArray = new ArrayList<>();

                // Check if there are classes for the current course
                if (groupedClasses.containsKey(yogaCourse.getId())) {
                    List<YogaClass> classes = groupedClasses.get(yogaCourse.getId());

                    // Iterate through each class and add its details to classList
                    for (YogaClass yogaClass : classes) {
                        Map<String, Object> classObject = new HashMap<>();
                        classObject.put("date", yogaClass.getDate());
                        classObject.put("teacher", yogaClass.getTeacherName());
                        classObject.put("comments", yogaClass.getComments());
                        // Add other fields if needed
                        classListArray.add(classObject);
                    }
                }

                // Add classList to the courseObject
                courseObject.put("classList", classListArray);
                // Add the courseObject to detailListArray
                detailListArray.add(courseObject);
            }

            // Add detailList to the combinedJson
            combinedJson.put("detailList", detailListArray);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return combinedJson;
    }

    private String sendPostRequest(String jsonpayload) throws Exception {
        URL url = new URL(getString(R.string.url));
        trustAllHosts();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonpayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the response from the server
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                Looper.prepare();
                Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
                Looper.loop();
                return "Upload successful. Server response: " + br.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } else {
            return "Upload failed. Server response code: " + responseCode;
        }
    }

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
            if (cursor != null) {
                cursor.close();
            }
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

    private class UploadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String jsonPayload = params[0];
            try {
                return sendPostRequest(jsonPayload);
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

    private void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        dbHelper.close(); // Close the database
        super.onDestroy();
    }

}
