package com.example.uya;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import java.util.Calendar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText startTime;
    private EditText endTime;
    private Spinner dayOfWeek;
    private EditText capacity;
    private EditText duration;
    private EditText pricePerClass;

    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dayOfWeek = findViewById(R.id.dayOfWeek);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        capacity = findViewById(R.id.capacity);
        duration = findViewById(R.id.duration);
        pricePerClass = findViewById(R.id.pricePerClass);


        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(startTime);
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(endTime);
            }
        });
    }

    private void showTimePickerDialog(final EditText editText) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                        editText.setText(time);
                    }
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void validateAndSubmitForm() {

        boolean isValid = true;

         //Check if each field is empty
        if (startTime.getText().toString().trim().isEmpty()) {
            startTime.setError("Start Time is required");
            isValid = false;
      }

       if (endTime.getText().toString().trim().isEmpty()) {
        endTime.setError("End Time is required");
            isValid = false;
        }
        if (capacity.getText().toString().trim().isEmpty()) {
            capacity.setError("Capacity is required");
            isValid = false;
        }
        if (duration.getText().toString().trim().isEmpty()) {
            duration.setError("Duration is required");
            isValid = false;
        }
        if (pricePerClass.getText().toString().trim().isEmpty()) {
            pricePerClass.setError("price is required");
            isValid = false;
        }
        // Add more field validation checks for other form fields as needed

        if (isValid) {
            // All fields have values, proceed with form submission
            // Implement your submission logic here
            // For example, you can send the form data to a server or perform some action
            Toast.makeText(this, "Form submitted successfully", Toast.LENGTH_SHORT).show();
        }
    }


}