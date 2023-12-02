package com.example.uya;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uya.model.YogaClass;

import java.util.List;

public class ClassListActivity extends AppCompatActivity {

    private Spinner dayOfWeekSpinner;
    private RecyclerView recyclerView;
    private MyDatabaseHelper databaseHelper;
    private ClassListAdapter classListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        databaseHelper = new MyDatabaseHelper(this);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeek);
        recyclerView = findViewById(R.id.recyclerView);


        // Find the ImageView and set its OnClickListener
        ImageView imageViewHome = findViewById(R.id.homeButton);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateHome(v);
            }
        });
        // Configure the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classListAdapter = new ClassListAdapter();
        recyclerView.setAdapter(classListAdapter);

        // Populate the Spinner with the list of days
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);

        // Add a listener to the Spinner to detect when the selected day changes
        dayOfWeekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle the selection change
                String selectedDay = dayOfWeekSpinner.getSelectedItem().toString();
                populateList(selectedDay); // Query and populate the list based on the selected day
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    private void populateList(String selectedDay) {
        // Query the database for data from TABLE_YOGA_CLASS based on the selected day
        List<YogaClass> yogaClasses = databaseHelper.queryClassesForDay(selectedDay);

        // Log the selected day
        Log.d("ClassList", "Selected Day: " + selectedDay);

            classListAdapter.setData(yogaClasses);
            classListAdapter.notifyDataSetChanged();


    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmation(int classId) {
        new AlertDialog.Builder(ClassListActivity.this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this Class?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    try {
                        // Execute the DELETE statement
                        db.delete("yoga_class", MyDatabaseHelper.ID + "=?", new String[]{String.valueOf(classId)});
                        showToast("Class deleted successfully");
                        // After deletion, refresh the list
                        populateList(dayOfWeekSpinner.getSelectedItem().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        db.close();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User clicked "No," do nothing, or you can add additional logic
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // RecyclerView Adapter for the YogaClass items
    private class ClassListAdapter extends RecyclerView.Adapter<ClassListAdapter.ViewHolder> {

        private List<YogaClass> data;

        public void setData(List<YogaClass> data) {
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.activity_class_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (data == null || data.isEmpty()) {
                return; // Do nothing if the data is null or empty
            }

            YogaClass yogaClass = data.get(position);

            // Set data to views in the CardView layout
            if (holder.dateTextView != null) {
                holder.dateTextView.setText("Date:" +yogaClass.getDate());
            }
            if (holder.timeTextView != null) {
                holder.timeTextView.setText("Time:"+yogaClass.getTimeOfCourse());
            }
            if (holder.teacherTextView != null) {
                holder.teacherTextView.setText("Teacher:"+yogaClass.getTeacherName());
            }
            if (holder.commentsTextView != null) {
                holder.commentsTextView.setText(TextUtils.isEmpty(yogaClass.getComments()) ? "Additional Comments:----" : "Additional Comments:"+ yogaClass.getComments());
            }

            // Set click listener for delete icon
            if (holder.deleteImageView != null) {
                holder.deleteImageView.setOnClickListener(v -> showDeleteConfirmation(yogaClass.getId()));
            }
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        // ViewHolder class for the RecyclerView
        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView dateTextView;
            public TextView timeTextView;
            public TextView teacherTextView;
            public TextView commentsTextView;
            public ImageView deleteImageView;

            public ViewHolder(View itemView) {
                super(itemView);

                dateTextView = itemView.findViewById(R.id.textViewDateItem);
                timeTextView = itemView.findViewById(R.id.textViewTimeItem);
                teacherTextView = itemView.findViewById(R.id.textViewTeacherItem);
                commentsTextView = itemView.findViewById(R.id.textViewAdditionalCommentItem);
                deleteImageView = itemView.findViewById(R.id.imageViewDelete);
            }
        }
    }
    public void navigateHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
