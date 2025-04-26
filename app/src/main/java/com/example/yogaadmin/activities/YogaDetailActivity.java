package com.example.yogaadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaadmin.R;
import com.example.yogaadmin.activities.ClassInstanceActivity;
import com.example.yogaadmin.database.DatabaseHelper;
import com.example.yogaadmin.models.YogaClass;

public class YogaDetailActivity extends AppCompatActivity {

    private YogaClass yogaClass;
    private DatabaseHelper databaseHelper;
    private long classId;

    // UI Elements
    private TextView tvDayOfWeek;
    private TextView tvTime;
    private TextView tvType;
    private TextView tvTeacher;
    private TextView tvCapacity;
    private TextView tvDuration;
    private TextView tvPrice;
    private TextView tvEquipment;
    private TextView tvDescription;
    private Button btnDeleteClass;
    private Button btnViewInstances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_detail);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get yoga class ID from intent
        classId = getIntent().getLongExtra("yoga_class_id", -1);
        if (classId == -1) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load yoga class details
        yogaClass = databaseHelper.getYogaClassById(classId);
        if (yogaClass == null) {
            Toast.makeText(this, "Error: Class not found in database", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Yoga Class Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        initializeViews();

        // Setup button click listeners
        setupListeners();

        // Display yoga class details
        displayYogaClassDetails();
    }

    private void initializeViews() {
        tvDayOfWeek = findViewById(R.id.tvDetailDay);
        tvTime = findViewById(R.id.tvDetailTime);
        tvType = findViewById(R.id.tvDetailType);
        tvTeacher = findViewById(R.id.tvDetailTeacher);
        tvCapacity = findViewById(R.id.tvDetailCapacity);
        tvDuration = findViewById(R.id.tvDetailDuration);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvEquipment = findViewById(R.id.tvDetailEquipment);
        tvDescription = findViewById(R.id.tvDetailDescription);
        btnDeleteClass = findViewById(R.id.btnDeleteClass);
        btnViewInstances = findViewById(R.id.btnViewInstances);
    }

    private void setupListeners() {
        // Set click listener for delete button
        btnDeleteClass.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        // Set click listener for view instances button
        btnViewInstances.setOnClickListener(v -> {
            Intent intent = new Intent(YogaDetailActivity.this, ClassInstanceActivity.class);
            intent.putExtra("yoga_class_id", classId);
            startActivity(intent);
        });
    }

    private void displayYogaClassDetails() {
        // Set text for each field
        tvDayOfWeek.setText(yogaClass.getDayOfWeek());
        tvTime.setText(yogaClass.getCourseTime());
        tvType.setText(yogaClass.getClassType());
        tvTeacher.setText(yogaClass.getTeacher());
        tvCapacity.setText(String.valueOf(yogaClass.getCapacity()));
        tvDuration.setText(String.valueOf(yogaClass.getDuration()) + " mins");
        tvPrice.setText("£" + String.format("%.2f", yogaClass.getPricePerClass()));

        // Handle optional fields
        String equipment = yogaClass.getEquipmentNeeded();
        String description = yogaClass.getDescription();

        tvEquipment.setText(equipment != null && !equipment.isEmpty() ? equipment : "None");
        tvDescription.setText(description != null && !description.isEmpty() ? description : "No description available");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Yoga Class")
                .setMessage("Are you sure you want to delete this yoga class?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteYogaClass();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteYogaClass() {
        // Delete the class from database
        databaseHelper.deleteYogaClass(classId);
        Toast.makeText(this, "Yoga class deleted", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity and go back to the list
    }
}