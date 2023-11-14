package com.example.uya;

import static com.example.uya.MyDatabaseHelper.CAPACITY;
import static com.example.uya.MyDatabaseHelper.COURSE_TIME;
import static com.example.uya.MyDatabaseHelper.DAY;
import static com.example.uya.MyDatabaseHelper.DESCRIPTION;
import static com.example.uya.MyDatabaseHelper.DURATION;
import static com.example.uya.MyDatabaseHelper.ID;
import static com.example.uya.MyDatabaseHelper.PRICE;
import static com.example.uya.MyDatabaseHelper.YOGA_TYPE;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uya.model.YogaCourse;

import java.util.ArrayList;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_list_layout);

        CourseList courseList = new CourseList(this);
        List<YogaCourse> data = courseList.fetchData();

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
                    courseList.deleteCourse(courseId);
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

    public class CourseList {
        private Context context;
        private MyDatabaseHelper databaseHelper;

        public CourseList(Context context) {
            this.context = context;
            databaseHelper = new MyDatabaseHelper(context);
        }

        public List<YogaCourse> fetchData() {
            List<YogaCourse> yogaCourses = new ArrayList<>();

            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = null;
            try {
                String query = "SELECT * FROM course_details ORDER BY " + ID + " DESC";
                cursor = db.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        // Extract data from the cursor and create YogaCourse objects
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
                        String day = cursor.getString(cursor.getColumnIndexOrThrow(DAY));
                        String duration = cursor.getString(cursor.getColumnIndexOrThrow(DURATION));
                        int price = cursor.getInt(cursor.getColumnIndexOrThrow(PRICE));
                        String timeOfCourse = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_TIME));
                        String yogaType = cursor.getString(cursor.getColumnIndexOrThrow(YOGA_TYPE));
                        String capacity = cursor.getString(cursor.getColumnIndexOrThrow(CAPACITY));
                        String desc = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION));

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
                db.close();
            }
            return yogaCourses;
        }

        public void deleteCourse(int courseId) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            try {
                // Execute the DELETE statement
                db.delete("course_details", ID + "=?", new String[]{String.valueOf(courseId)});
                showToast("Course deleted successfully");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
        }

        private void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    public void navigateToHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class); // Replace HomeActivity with the name of your home screen activity
        startActivity(intent);
    }
}
