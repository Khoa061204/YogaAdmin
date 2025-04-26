package com.example.yogaadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaadmin.R;
import com.example.yogaadmin.database.DatabaseHelper;
import com.example.yogaadmin.models.YogaClass;

import java.util.List;

public class ViewClassesActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private ListView listViewClasses;
    private List<YogaClass> yogaClasses;

    // Search components
    private EditText etSearchTeacher;
    private Button btnSearch;
    private Button btnShowAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_classes);

        // Initialize Database Helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize UI components
        initializeComponents();

        // Load initial classes
        loadYogaClasses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload classes when coming back to this activity
        loadYogaClasses();
    }

    private void initializeComponents() {
        // Find ListView
        listViewClasses = findViewById(R.id.listViewClasses);

        // Find search components
        etSearchTeacher = findViewById(R.id.etSearchTeacher);
        btnSearch = findViewById(R.id.btnSearch);
        btnShowAll = findViewById(R.id.btnShowAll);

        // Search button listener
        btnSearch.setOnClickListener(v -> performSearch());

        // Show all button listener
        btnShowAll.setOnClickListener(v -> loadYogaClasses());

        // Set item click listener to view details
        listViewClasses.setOnItemClickListener((parent, view, position, id) -> {
            YogaClass selectedClass = yogaClasses.get(position);
            openClassDetails(selectedClass.getId());
        });

        // Set item long click listener for delete functionality
        listViewClasses.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteConfirmationDialog(position);
            return true;
        });
    }

    private void loadYogaClasses() {
        // Clear search input
        etSearchTeacher.setText("");

        // Retrieve all yoga classes from database
        yogaClasses = databaseHelper.getAllYogaClasses();

        // Check if there are any classes
        if (yogaClasses.isEmpty()) {
            Toast.makeText(this, "No yoga classes found", Toast.LENGTH_SHORT).show();
            listViewClasses.setAdapter(null);
            return;
        }

        // Create custom adapter for yoga classes
        YogaClassAdapter adapter = new YogaClassAdapter(yogaClasses);
        listViewClasses.setAdapter(adapter);
    }

    private void performSearch() {
        // Get search query
        String teacherName = etSearchTeacher.getText().toString().trim();

        // Validate input
        if (teacherName.isEmpty()) {
            etSearchTeacher.setError("Please enter a teacher name");
            etSearchTeacher.requestFocus();
            return;
        }

        // Perform search
        yogaClasses = databaseHelper.searchClassesByTeacher(teacherName);

        // Check search results
        if (yogaClasses.isEmpty()) {
            Toast.makeText(this, "No classes found for teacher: " + teacherName, Toast.LENGTH_SHORT).show();
            listViewClasses.setAdapter(null);
            return;
        }

        // Create and set adapter for search results
        YogaClassAdapter adapter = new YogaClassAdapter(yogaClasses);
        listViewClasses.setAdapter(adapter);
    }

    // Method to open class details activity
    private void openClassDetails(long classId) {
        Intent intent = new Intent(ViewClassesActivity.this, YogaDetailActivity.class);
        intent.putExtra("yoga_class_id", classId);
        startActivity(intent);
    }


    private void showDeleteConfirmationDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Yoga Class")
                .setMessage("Are you sure you want to delete this yoga class?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Get the class to be deleted
                    YogaClass classToDelete = yogaClasses.get(position);

                    // Delete from database
                    databaseHelper.deleteYogaClass(classToDelete.getId());

                    // Reload the entire list to ensure UI consistency
                    loadYogaClasses();

                    // Show confirmation toast
                    Toast.makeText(ViewClassesActivity.this,
                            "Yoga class deleted successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Custom adapter for displaying yoga classes
    private class YogaClassAdapter extends ArrayAdapter<YogaClass> {
        public YogaClassAdapter(List<YogaClass> classes) {
            super(ViewClassesActivity.this, R.layout.item_yoga_class, classes);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Inflate the view if not already inflated
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_yoga_class, parent, false);
            }

            // Get the current yoga class
            YogaClass currentClass = getItem(position);

            // Populate views
            TextView tvClassDetails = convertView.findViewById(R.id.tvClassDetails);

            if (currentClass != null) {
                // Format class details
                String classDetails = String.format(
                        "Day: %s | Time: %s | Type: %s\n" +
                                "Capacity: %d | Duration: %d mins | Price: £%.2f\n" +
                                "Teacher: %s",
                        currentClass.getDayOfWeek(),
                        currentClass.getCourseTime(),
                        currentClass.getClassType(),
                        currentClass.getCapacity(),
                        currentClass.getDuration(),
                        currentClass.getPricePerClass(),
                        currentClass.getTeacher()
                );

                tvClassDetails.setText(classDetails);
            }

            return convertView;
        }
    }
}