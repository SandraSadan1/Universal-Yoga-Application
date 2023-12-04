package com.example.uya;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uya.model.YogaCourse;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourseDetailsAdapter adapter;
    private List<YogaCourse> yogaCourses;
    private MyDatabaseHelper dbHelper;

    // Views for the CardView
    private CardView cardViewItem;
    private TextView textViewDay, texttime, textduration, textprice, textcapacity, texttype, textdescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        dbHelper = new MyDatabaseHelper(this);

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
        db.close();

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


}
