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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.uya.model.YogaCourse;
import java.util.ArrayList;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_list_layout);
        // Your activity code here

        CourseList courseList = new CourseList(this);
        List<YogaCourse> data = courseList.fetchData();
        // Process the data as needed
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
                String query = "SELECT * FROM course_details";
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
    }
}
