package com.example.yogaadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.yogaadmin.R;
import com.example.yogaadmin.database.DatabaseHelper;
import com.example.yogaadmin.network.CloudUploadService;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DatabaseHelper databaseHelper;
    private CloudUploadService cloudUploadService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase with the specific URL
        try {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://comp1786-database-default-rtdb.asia-southeast1.firebasedatabase.app/");
            Log.d(TAG, "Firebase initialized with custom URL");

            // Initialize database and network services
            databaseHelper = new DatabaseHelper(this);
            cloudUploadService = new CloudUploadService(this, firebaseDatabase);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to initialize Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Setup UI components
        setupButtons();
    }

    private void setupButtons() {
        // Find CardViews or Buttons for different actions
        CardView addClassCard = findViewById(R.id.cardAddClass);
        CardView viewClassesCard = findViewById(R.id.cardViewClasses);
        CardView uploadClassesCard = findViewById(R.id.cardUploadClasses);
        CardView resetDatabaseCard = findViewById(R.id.cardResetDatabase);

        // Null checks for each CardView
        if (addClassCard != null) {
            addClassCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddClassActivity.class);
                startActivity(intent);
            });
        }

        if (viewClassesCard != null) {
            viewClassesCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ViewClassesActivity.class);
                startActivity(intent);
            });
        }

        if (uploadClassesCard != null) {
            uploadClassesCard.setOnClickListener(v -> {
                // Check if Firebase is initialized
                if (cloudUploadService == null) {
                    Toast.makeText(this, "Firebase not initialized. Please restart the app.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Show confirmation dialog for upload with clear option
                showUploadOptionsDialog();
            });
        }

        if (resetDatabaseCard != null) {
            resetDatabaseCard.setOnClickListener(v -> {
                // Show confirmation dialog before resetting
                showConfirmationDialog();
            });
        }
    }

    private void showUploadOptionsDialog() {
        // Create choices for the dialog
        final CharSequence[] options = {"Upload (Append to existing data)", "Upload (Clear existing data first)"};

        new AlertDialog.Builder(this)
                .setTitle("Upload Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Append data
                            performUpload(false);
                            break;
                        case 1: // Clear and upload
                            performUpload(true);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performUpload(boolean clearFirst) {
        // Check network availability
        if (!cloudUploadService.isNetworkAvailable()) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting upload process, clearFirst=" + clearFirst);

        // Get classes from database
        var yogaClasses = databaseHelper.getAllYogaClasses();
        if (yogaClasses == null || yogaClasses.isEmpty()) {
            Toast.makeText(this, "No classes to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Found " + yogaClasses.size() + " classes to upload");

        // Create callback for upload
        CloudUploadService.UploadCallback callback = new CloudUploadService.UploadCallback() {
            @Override
            public void onUploadSuccess() {
                Log.d(TAG, "Upload completed successfully");
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Classes uploaded successfully",
                                Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onUploadFailed(String errorMessage) {
                Log.e(TAG, "Upload failed: " + errorMessage);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Upload failed: " + errorMessage,
                                Toast.LENGTH_LONG).show()
                );
            }
        };

        // Perform the upload with or without clearing
        if (clearFirst) {
            cloudUploadService.clearAndUploadYogaClasses(yogaClasses, callback);
        } else {
            cloudUploadService.uploadYogaClasses(yogaClasses, callback);
        }
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Database")
                .setMessage("Are you sure you want to reset the entire database? This cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    // Perform database reset
                    databaseHelper.resetDatabase();
                    Toast.makeText(this, "Database has been reset", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}