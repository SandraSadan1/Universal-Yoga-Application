package com.example.uya;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uya.model.YogaClass;

import java.util.List;

public class ClassList extends AppCompatActivity {
    private Spinner dayOfWeekSpinner;
    private TableLayout tableLayout;
    private MyDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        databaseHelper = new MyDatabaseHelper(this);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeek);
        tableLayout = findViewById(R.id.tableLayout);

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
                populateTable(selectedDay); // Query and populate the table based on the selected day
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    private void populateTable(String selectedDay) {
        // Query the database for data from TABLE_YOGA_CLASS based on the selected day
        List<YogaClass> yogaClasses = databaseHelper.queryClassesForDay(selectedDay);

        // Log the selected day
        Log.d("ClassList", "Selected Day: " + selectedDay);

        // Get a reference to the existing TableLayout
        TableLayout tableLayout = findViewById(R.id.tableLayout);

        // Clear existing data rows in the table
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        // Log the number of rows returned by the query
        Log.d("ClassList", "Number of rows returned: " + yogaClasses.size());

        // Dynamically populate the TableLayout with data from the database
        for (YogaClass yogaClass : yogaClasses) {
            TableRow tableRow = new TableRow(this);

            // Add TextViews for each column
            TextView dateTextView = createTextView(yogaClass.getDate());
            dateTextView.setGravity(Gravity.CENTER);

            TextView timeTextView = createTextView(yogaClass.getTimeOfCourse());
            timeTextView.setGravity(Gravity.CENTER);

            TextView teacherTextView = createTextView(yogaClass.getTeacherName());
            teacherTextView.setGravity(Gravity.CENTER);

            TextView commentsTextView = createTextView(yogaClass.getComments());
            commentsTextView.setGravity(Gravity.CENTER);
            if (TextUtils.isEmpty(yogaClass.getComments())) {
                commentsTextView.setText("-------");
            } else {
                commentsTextView.setText(yogaClass.getComments());
            }

            LinearLayout actionLayout = new LinearLayout(this);
            actionLayout.setOrientation(LinearLayout.HORIZONTAL);
            actionLayout.setGravity(Gravity.CENTER);

            ImageView deleteImageView = new ImageView(this);
            deleteImageView.setImageResource(R.drawable.delete);
            deleteImageView.setLayoutParams(new ViewGroup.LayoutParams(52, 52));
            actionLayout.addView(deleteImageView);

            // Get the class ID
            final int classId = yogaClass.getId();
            // Set click listener for delete icon
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create and show an AlertDialog to confirm deletion
                    new AlertDialog.Builder(ClassList.this)
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to delete this Yoga class?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                                    try {
                                        // Execute the DELETE statement
                                        db.delete("yoga_class", MyDatabaseHelper.ID + "=?", new String[]{String.valueOf(classId)});
                                        showToast("Class deleted successfully");
                                        // After deletion, refresh the table
                                        populateTable(dayOfWeekSpinner.getSelectedItem().toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        db.close();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // User clicked "No," do nothing, or you can add additional logic
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

            // Add TextViews to the TableRow
            tableRow.addView(dateTextView);
            tableRow.addView(timeTextView);
            tableRow.addView(teacherTextView);
            tableRow.addView(commentsTextView);
            tableRow.addView(actionLayout);

            // Add the TableRow to the existing TableLayout
            tableLayout.addView(tableRow);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public void navigateToHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class); // Replace HomeActivity with the name of your home screen activity
        startActivity(intent);
    }


    private TextView createTextView(String text) {
        // Helper method to create a TextView with common properties
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10);
        return textView;
    }
}
