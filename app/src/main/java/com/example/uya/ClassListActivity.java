package com.example.uya;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uya.model.YogaClass;
import com.example.uya.model.YogaCourse;

import java.util.List;

public class ClassListActivity extends AppCompatActivity {

    private Spinner dayOfWeekSpinner;
    private RecyclerView recyclerView;
    private CardView cardView;
    private MyDatabaseHelper databaseHelper;
    private ClassListAdapter classListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        databaseHelper = new MyDatabaseHelper(this);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeek);
        recyclerView = findViewById(R.id.recyclerView);
        cardView = findViewById(R.id.cardViewItem);

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

        // Initialize classListAdapter here
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

        // Call nodata() after initializing classListAdapter
       nodata();
    }

    private void populateList(String selectedDay) {
        // Query the database for data from TABLE_YOGA_CLASS based on the selected day
        List<YogaClass> yogaClasses = databaseHelper.queryClassesForDay(selectedDay);
        // Log the selected day
        Log.d("ClassList", "Selected Day: " + selectedDay);
        classListAdapter.setData(yogaClasses);
        classListAdapter.notifyDataSetChanged();
        nodata();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isNoDataViewAdded = false;
    private CardView noDataCardView;  // Declare as a class-level variable

    private void nodata() {
        String selectedDay = dayOfWeekSpinner.getSelectedItem().toString();
        List<YogaClass> data = databaseHelper.queryClassesForDay(selectedDay);

        // Initialize the CardView if not already initialized
        if (noDataCardView == null) {
            noDataCardView = new CardView(this);
            TextView noDataTextView = new TextView(this);
            noDataTextView.setText("No Data Available");
            noDataTextView.setGravity(Gravity.CENTER);  // Center the text vertically and horizontally
            noDataTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            noDataTextView.setTextColor(ContextCompat.getColor(this, R.color.redColor));
            noDataCardView.setPaddingRelative(300, 100, 10, 50);
            noDataCardView.addView(noDataTextView);
        }

        if (data.isEmpty()) {
            if (!isNoDataViewAdded) {
                // Add the CardView to the layout and set layout parameters to center it
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.gravity = Gravity.CENTER;
                ((ViewGroup) recyclerView.getParent()).addView(noDataCardView, layoutParams);

                Log.d("ClassList", "No data available. Hiding recyclerView.");
                cardView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                isNoDataViewAdded = true;
            }
        } else {
            if (isNoDataViewAdded) {
                // Remove the CardView if it was added
                ViewGroup parent = (ViewGroup) recyclerView.getParent();
                if (parent != null && parent.indexOfChild(noDataCardView) != -1) {
                    parent.removeView(noDataCardView);
                }
                isNoDataViewAdded = false;
            }

            // Show the recyclerView
            recyclerView.setVisibility(View.VISIBLE);

            // Set up the adapter when data is available
            classListAdapter.setData(data);
            classListAdapter.notifyDataSetChanged();
        }
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
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                YogaClass yogaClass = data.get(position);
                // Set data to views in the CardView layout
                if (holder.dateTextView != null) {
                    holder.dateTextView.setText("Date:" + yogaClass.getDate());
                }
                if (holder.timeTextView != null) {
                    holder.timeTextView.setText("Time:" + yogaClass.getTimeOfCourse());
                }
                if (holder.teacherTextView != null) {
                    holder.teacherTextView.setText("Teacher:" + yogaClass.getTeacherName());
                }
                if (holder.commentsTextView != null) {
                    holder.commentsTextView.setText(TextUtils.isEmpty(yogaClass.getComments()) ? "Additional Comments:----" : "Additional Comments:" + yogaClass.getComments());
                }

                // Set click listener for delete icon
                if (holder.deleteImageView != null) {
                    holder.deleteImageView.setOnClickListener(v -> showDeleteConfirmation(yogaClass.getId()));
                }
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
