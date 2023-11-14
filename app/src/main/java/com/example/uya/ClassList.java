package com.example.uya;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
            TextView timeTextView = createTextView(yogaClass.getTimeOfCourse());
            TextView teacherTextView = createTextView(yogaClass.getTeacherName());
            TextView commentsTextView = createTextView(yogaClass.getComments());

            // Add TextViews to the TableRow
            tableRow.addView(dateTextView);
            tableRow.addView(timeTextView);
            tableRow.addView(teacherTextView);
            tableRow.addView(commentsTextView);

            // Add the TableRow to the existing TableLayout
            tableLayout.addView(tableRow);
        }
    }



    private TextView createTextView(String text) {
        // Helper method to create a TextView with common properties
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10);
        return textView;
    }
}
