package com.example.yogaadmin.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.yogaadmin.models.YogaClass;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CloudUploadService {
    private static final String TAG = "CloudUploadService";
    private Context context;
    private DatabaseReference databaseReference;

    // Constructor with FirebaseDatabase parameter
    public CloudUploadService(Context context, FirebaseDatabase database) {
        this.context = context;
        try {
            // Use the provided Firebase database instance
            databaseReference = database.getReference("yoga_classes");
            Log.d(TAG, "Firebase initialized successfully with custom URL");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }

    // Legacy constructor for backward compatibility
    public CloudUploadService(Context context) {
        this.context = context;
        try {
            // Initialize Firebase Realtime Database with specific URL
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://comp1786-database-default-rtdb.asia-southeast1.firebasedatabase.app/");
            databaseReference = database.getReference("yoga_classes");
            Log.d(TAG, "Firebase initialized successfully with hardcoded URL");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }

    // Check network connectivity
    public boolean isNetworkAvailable() {
        Log.d(TAG, "Checking network availability");
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null");
            return false;
        }

        // For Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                Log.e(TAG, "Active network is null");
                return false;
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            boolean hasInternet = capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
            Log.d(TAG, "Network available: " + hasInternet);
            return hasInternet;
        }
        // For older Android versions
        else {
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean hasInternet = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            Log.d(TAG, "Network available (legacy): " + hasInternet);
            return hasInternet;
        }
    }

    // Method to clear all data and then upload
    public void clearAndUploadYogaClasses(List<YogaClass> yogaClasses, UploadCallback callback) {
        Log.d(TAG, "clearAndUploadYogaClasses called - clearing database first");

        // Check if Firebase was properly initialized
        if (databaseReference == null) {
            Log.e(TAG, "Firebase database reference is null - initialization failed");
            if (callback != null) {
                callback.onUploadFailed("Firebase initialization failed. Please check your configuration.");
            }
            return;
        }

        // Check network availability
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No network connection available");
            if (callback != null) {
                callback.onUploadFailed("No network connection");
            }
            return;
        }

        // Clear the database first
        databaseReference.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Database cleared successfully, now uploading new data");
                    // After clearing, upload the new data
                    uploadYogaClasses(yogaClasses, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to clear database: " + e.getMessage(), e);
                    if (callback != null) {
                        callback.onUploadFailed("Failed to clear database: " + e.getMessage());
                    }
                });
    }

    // Upload yoga classes to cloud service
    public void uploadYogaClasses(List<YogaClass> yogaClasses, UploadCallback callback) {
        Log.d(TAG, "uploadYogaClasses called with " + (yogaClasses != null ? yogaClasses.size() : "null") + " classes");

        // Check if Firebase was properly initialized
        if (databaseReference == null) {
            Log.e(TAG, "Firebase database reference is null - initialization failed");
            if (callback != null) {
                callback.onUploadFailed("Firebase initialization failed. Please check your configuration.");
            }
            return;
        }

        // Check network availability
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No network connection available");
            if (callback != null) {
                callback.onUploadFailed("No network connection");
            }
            return;
        }

        // Check if classes list is empty
        if (yogaClasses == null || yogaClasses.isEmpty()) {
            Log.e(TAG, "No classes to upload");
            if (callback != null) {
                callback.onUploadFailed("No classes to upload");
            }
            return;
        }

        // Track upload status
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);
        final StringBuilder errorMessages = new StringBuilder();
        final int totalClasses = yogaClasses.size();

        // Log all classes for debugging
        Log.d(TAG, "Classes to upload:");
        for (int i = 0; i < yogaClasses.size(); i++) {
            YogaClass cls = yogaClasses.get(i);
            Log.d(TAG, "Class " + i + ": " + cls.toString());
        }

        for (YogaClass yogaClass : yogaClasses) {
            try {
                // Handle classInstances to prevent serialization issues
                if (yogaClass.getClassInstances() != null) {
                    // Make a copy to avoid serialization problems
                    yogaClass.setClassInstances(new ArrayList<>(yogaClass.getClassInstances()));
                }

                // Use the unique ID as the key or generate a new push key
                String key = yogaClass.getId() > 0 ?
                        String.valueOf(yogaClass.getId()) :
                        databaseReference.push().getKey();

                if (key == null) {
                    Log.e(TAG, "Failed to generate Firebase key");
                    failCount.incrementAndGet();
                    errorMessages.append("Failed to generate key for a class. ");
                    checkCompletion(totalClasses, successCount.get(), failCount.get(), errorMessages.toString(), callback);
                    continue;
                }

                Log.d(TAG, "Uploading class with key: " + key);

                databaseReference.child(key).setValue(yogaClass)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Yoga class uploaded successfully: " + key);
                            successCount.incrementAndGet();
                            checkCompletion(totalClasses, successCount.get(), failCount.get(), errorMessages.toString(), callback);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to upload yoga class: " + e.getMessage(), e);
                            failCount.incrementAndGet();
                            errorMessages.append(e.getMessage()).append("; ");
                            checkCompletion(totalClasses, successCount.get(), failCount.get(), errorMessages.toString(), callback);
                        });
            } catch (Exception e) {
                Log.e(TAG, "Exception during upload process: " + e.getMessage(), e);
                failCount.incrementAndGet();
                errorMessages.append("Exception: ").append(e.getMessage()).append("; ");
                checkCompletion(totalClasses, successCount.get(), failCount.get(), errorMessages.toString(), callback);
            }
        }
    }

    // Helper method to check if all uploads are complete and trigger the callback
    private void checkCompletion(int total, int success, int fail, String errors, UploadCallback callback) {
        Log.d(TAG, "Checking completion: " + success + " successful, " + fail + " failed out of " + total);
        if (success + fail == total) {
            if (callback != null) {
                if (fail == 0) {
                    Log.d(TAG, "All uploads successful");
                    callback.onUploadSuccess();
                } else if (success == 0) {
                    Log.e(TAG, "All uploads failed: " + errors);
                    callback.onUploadFailed("All uploads failed. Errors: " + errors);
                } else {
                    Log.e(TAG, "Some uploads failed: " + fail + "/" + total + ". Errors: " + errors);
                    callback.onUploadFailed("Some uploads failed (" + fail + "/" + total + "). Errors: " + errors);
                }
            }
        }
    }

    // Callback interface for upload status
    public interface UploadCallback {
        void onUploadSuccess();
        void onUploadFailed(String errorMessage);
    }
}