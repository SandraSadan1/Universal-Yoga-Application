package com.example.uya;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private ImageView menuIcon;
    private boolean isPopupVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        menuIcon = findViewById(R.id.MenuIcon);

        menuIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPopupVisible) {
                    hidePopup();
                } else {
                    showPopup();
                }
            }
        });
    }

    public void showPopup() {
        String[] items = new String[]{"Add Course", "List Course", "Add Class", "List Class"};

        ListPopupWindow popup = new ListPopupWindow(this);

        // Create a custom adapter and set the text color programmatically
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, items) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                // Set the text color as per your requirement
                textView.setTextColor(Color.parseColor("#006400")); // Change Color.RED to the desired color
                return view;
            }
        };

        popup.setAdapter(adapter);
        popup.setAnchorView(menuIcon);
        popup.setHorizontalOffset(-menuIcon.getWidth());
        popup.setWidth(500);

        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = items[position];
                handlePopupItemClick(selectedItem, view);
                popup.dismiss();
            }
        });

        popup.show();
        isPopupVisible = true;
    }

    public void hidePopup() {
        isPopupVisible = false;
    }

    private void handlePopupItemClick(String selectedItem, View view) {
        if ("Add Course".equals(selectedItem)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if ("List Course".equals(selectedItem)) {
            Intent intent = new Intent(this, CourseListActivity.class);
            startActivity(intent);
        } else if ("Add Class".equals(selectedItem)) {
            Intent intent = new Intent(this, ClassInstances.class);
            startActivity(intent);
        } else if ("List Class".equals(selectedItem)) {
            Intent intent = new Intent(this, ClassListActivity.class);

            startActivity(intent);
        }
    }
        // Add conditions for other menu items as needed
    }

