package com.example.uya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.uya.model.TimeSlot;

import java.util.List;

public class TimeSlotAdapter extends ArrayAdapter<TimeSlot> {

    public TimeSlotAdapter(Context context, List<TimeSlot> timeSlots) {
        super(context, 0, timeSlots);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        TimeSlot timeSlot = getItem(position);

        if (timeSlot != null) {
            // Display the timeOfCourse in the TextView
            textView.setText(timeSlot.getCourseTime());
        }

        return convertView;
    }
}
