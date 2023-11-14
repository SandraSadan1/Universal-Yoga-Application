package com.example.uya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;
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
        popup.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, items));
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
        }
        else if ("List Course".equals(selectedItem)) {
            Intent intent = new Intent(this, CourseListActivity.class);
            startActivity(intent);
        } else if ("Add Class".equals(selectedItem)) {
            Intent intent = new Intent(this, ClassInstances.class);
            startActivity(intent);
        }
        else if ("List Class".equals(selectedItem)) {
            Intent intent = new Intent(this, ClassList.class);
            startActivity(intent);
        }

        // Add conditions for other menu items as needed
    }
}
