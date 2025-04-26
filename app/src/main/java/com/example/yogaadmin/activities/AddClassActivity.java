package com.example.yogaadmin.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaadmin.R;
import com.example.yogaadmin.database.DatabaseHelper;
import com.example.yogaadmin.models.YogaClass;
import com.example.yogaadmin.utils.InputValidator;

public class AddClassActivity extends AppCompatActivity {
    // UI Components
    private Spinner spinnerDayOfWeek;
    private EditText etCourseTime;
    private EditText etCapacity;
    private EditText etDuration;
    private EditText etPrice;
    private Spinner spinnerClassType;
    private EditText etDescription;
    private EditText etTeacherName;
    private Button btnSaveClass;

    // Database Helper
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        // Initialize Database Helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize UI Components
        initializeComponents();
    }

    private void initializeComponents() {
        // Find Views
        spinnerDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        etCourseTime = findViewById(R.id.etCourseTime);
        etCapacity = findViewById(R.id.etCapacity);
        etDuration = findViewById(R.id.etDuration);
        etPrice = findViewById(R.id.etPrice);
        spinnerClassType = findViewById(R.id.spinnerClassType);
        etDescription = findViewById(R.id.etDescription);
        etTeacherName = findViewById(R.id.etTeacherName); // Initialize teacher name field
        btnSaveClass = findViewById(R.id.btnSaveClass);

        // Setup Day of Week Spinner
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.days_of_week,
                android.R.layout.simple_spinner_item
        );
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        // Setup Class Type Spinner
        ArrayAdapter<CharSequence> classTypeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.class_types,
                android.R.layout.simple_spinner_item
        );
        classTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClassType.setAdapter(classTypeAdapter);

        // Save Class Button Listener
        btnSaveClass.setOnClickListener(v -> saveYogaClass());
    }

    private void saveYogaClass() {
        // Collect input values
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        String courseTime = etCourseTime.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String classType = spinnerClassType.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();
        String teacherName = etTeacherName.getText().toString().trim(); // Get teacher name

        // Validate inputs
        try {

            if (!InputValidator.isValidDayOfWeek(dayOfWeek)) {
                showError("Please select a valid day of week");
                return;
            }

            if (!InputValidator.isValidTimeFormat(courseTime)) {
                etCourseTime.setError("Invalid time format (HH:MM)");
                etCourseTime.requestFocus();
                return;
            }

            // Teacher name validation
            if (teacherName.isEmpty()) {
                etTeacherName.setError("Teacher name is required");
                etTeacherName.requestFocus();
                return;
            }

            int capacity = Integer.parseInt(capacityStr);
            if (!InputValidator.isValidCapacity(capacity)) {
                etCapacity.setError("Invalid capacity (1-50)");
                etCapacity.requestFocus();
                return;
            }

            int duration = Integer.parseInt(durationStr);
            if (!InputValidator.isValidDuration(duration)) {
                etDuration.setError("Invalid duration (1-180 minutes)");
                etDuration.requestFocus();
                return;
            }

            double price = Double.parseDouble(priceStr);
            if (!InputValidator.isValidPrice(price)) {
                etPrice.setError("Invalid price (0-100)");
                etPrice.requestFocus();
                return;
            }

            if (!InputValidator.isValidClassType(classType)) {
                showError("Please select a valid class type");
                return;
            }

            // Create YogaClass object
            YogaClass yogaClass = new YogaClass();
            yogaClass.setDayOfWeek(dayOfWeek);
            yogaClass.setCourseTime(courseTime);
            yogaClass.setCapacity(capacity);
            yogaClass.setDuration(duration);
            yogaClass.setPricePerClass(price);
            yogaClass.setClassType(classType);
            yogaClass.setDescription(description);
            yogaClass.setTeacher(teacherName); // Set teacher name

            // Save to database
            long id = databaseHelper.insertYogaClass(yogaClass);

            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Class Added")
                    .setMessage("Yoga class added successfully. Would you like to add another class?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear form for next entry
                        clearForm();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Return to main activity
                        finish();
                    })
                    .show();

        } catch (NumberFormatException e) {
            showError("Please enter valid numeric values for capacity, duration, and price");
        }
    }

    private void clearForm() {
        spinnerDayOfWeek.setSelection(0);
        etCourseTime.setText("");
        etCapacity.setText("");
        etDuration.setText("");
        etPrice.setText("");
        spinnerClassType.setSelection(0);
        etDescription.setText("");
        etTeacherName.setText(""); // Clear teacher name

        // Set focus to first input
        etCourseTime.requestFocus();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}