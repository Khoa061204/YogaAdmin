package com.example.yogaadmin.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaadmin.R;
import com.example.yogaadmin.database.DatabaseHelper;
import com.example.yogaadmin.models.ClassInstance;
import com.example.yogaadmin.models.YogaClass;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClassInstanceActivity extends AppCompatActivity {

    private static final String TAG = "ClassInstancesActivity";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.US);

    private DatabaseHelper databaseHelper;
    private ListView listViewInstances;
    private TextView tvNoInstances;
    private List<ClassInstance> classInstances;
    private YogaClass yogaClass;
    private long yogaClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_instances);

        // Get yoga class ID from intent
        yogaClassId = getIntent().getLongExtra("yoga_class_id", -1);
        if (yogaClassId == -1) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Get yoga class
        yogaClass = databaseHelper.getYogaClassById(yogaClassId);
        if (yogaClass == null) {
            Toast.makeText(this, "Error: Class not found in database", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Class Instances");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components
        setupUI();

        // Load class instances
        loadClassInstances();
    }

    private void setupUI() {
        // Set up class info
        TextView tvClassTitle = findViewById(R.id.tvClassTitle);
        TextView tvClassSchedule = findViewById(R.id.tvClassSchedule);

        tvClassTitle.setText(yogaClass.getClassType());
        tvClassSchedule.setText(yogaClass.getDayOfWeek() + " at " + yogaClass.getCourseTime());

        // Set up list view
        listViewInstances = findViewById(R.id.listViewInstances);
        tvNoInstances = findViewById(R.id.tvNoInstances);

        // Set up add button
        Button btnAddInstance = findViewById(R.id.btnAddInstance);
        btnAddInstance.setOnClickListener(v -> showAddInstanceDialog());

        // Set up list item click listeners
        listViewInstances.setOnItemClickListener((parent, view, position, id) -> {
            ClassInstance instance = classInstances.get(position);
            showInstanceOptionsDialog(instance);
        });
    }

    private void loadClassInstances() {
        // Get instances for this yoga class
        classInstances = databaseHelper.getClassInstancesByYogaClassId(yogaClassId);

        if (classInstances.isEmpty()) {
            tvNoInstances.setVisibility(View.VISIBLE);
            listViewInstances.setVisibility(View.GONE);
            return;
        }

        tvNoInstances.setVisibility(View.GONE);
        listViewInstances.setVisibility(View.VISIBLE);

        // Create and set adapter
        ClassInstanceAdapter adapter = new ClassInstanceAdapter();
        listViewInstances.setAdapter(adapter);
    }

    private void showAddInstanceDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_instance, null);

        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        EditText etTeacher = dialogView.findViewById(R.id.etInstanceTeacher);
        EditText etComments = dialogView.findViewById(R.id.etInstanceComments);

        // Set default teacher from yoga class
        etTeacher.setText(yogaClass.getTeacher());

        // Set up date picker
        final Calendar calendar = Calendar.getInstance();
        final Date[] selectedDate = {null};

        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        selectedDate[0] = calendar.getTime();
                        tvSelectedDate.setText(DATE_FORMAT.format(selectedDate[0]));

                        // Verify day of week matches
                        validateSelectedDate(selectedDate[0], yogaClass.getDayOfWeek());
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Show dialog
        new AlertDialog.Builder(this)
                .setTitle("Add Class Instance")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // Validate input
                    if (selectedDate[0] == null) {
                        Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String teacher = etTeacher.getText().toString().trim();
                    if (teacher.isEmpty()) {
                        Toast.makeText(this, "Teacher name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Handle day of week mismatch
                    if (!isDayOfWeekMatch(selectedDate[0], yogaClass.getDayOfWeek())) {
                        new AlertDialog.Builder(this)
                                .setTitle("Date Mismatch")
                                .setMessage("The selected date (" + DATE_FORMAT.format(selectedDate[0]) +
                                        ") is not a " + yogaClass.getDayOfWeek() + ". Do you want to continue anyway?")
                                .setPositiveButton("Yes", (dialog2, which2) -> {
                                    // Create instance
                                    createClassInstance(selectedDate[0], teacher,
                                            etComments.getText().toString().trim());
                                })
                                .setNegativeButton("No", null)
                                .show();
                    } else {
                        // Create instance
                        createClassInstance(selectedDate[0], teacher,
                                etComments.getText().toString().trim());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void validateSelectedDate(Date date, String requiredDayOfWeek) {
        if (!isDayOfWeekMatch(date, requiredDayOfWeek)) {
            Toast.makeText(this,
                    "Note: Selected date is not a " + requiredDayOfWeek,
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean isDayOfWeekMatch(Date date, String requiredDayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String dayOfWeekString = getDayOfWeekString(dayOfWeek);

        return dayOfWeekString.equalsIgnoreCase(requiredDayOfWeek);
    }

    private String getDayOfWeekString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:    return "Sunday";
            case Calendar.MONDAY:    return "Monday";
            case Calendar.TUESDAY:   return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY:  return "Thursday";
            case Calendar.FRIDAY:    return "Friday";
            case Calendar.SATURDAY:  return "Saturday";
            default:                 return "";
        }
    }

    private void createClassInstance(Date date, String teacher, String comments) {
        ClassInstance instance = new ClassInstance(yogaClassId, date, teacher, comments);

        long id = databaseHelper.insertClassInstance(instance);
        if (id > 0) {
            Toast.makeText(this, "Class instance added", Toast.LENGTH_SHORT).show();
            loadClassInstances();  // Refresh list
        } else {
            Toast.makeText(this, "Failed to add class instance", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInstanceOptionsDialog(ClassInstance instance) {
        String[] options = {"Edit", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle("Instance Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:  // Edit
                            showEditInstanceDialog(instance);
                            break;
                        case 1:  // Delete
                            showDeleteConfirmationDialog(instance);
                            break;
                    }
                })
                .show();
    }

    private void showEditInstanceDialog(ClassInstance instance) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_instance, null);

        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        EditText etTeacher = dialogView.findViewById(R.id.etInstanceTeacher);
        EditText etComments = dialogView.findViewById(R.id.etInstanceComments);

        // Set current values
        tvSelectedDate.setText(DATE_FORMAT.format(instance.getDate()));
        etTeacher.setText(instance.getTeacher());
        etComments.setText(instance.getComments());

        // Set up date picker
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(instance.getDate());
        final Date[] selectedDate = {instance.getDate()};

        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        selectedDate[0] = calendar.getTime();
                        tvSelectedDate.setText(DATE_FORMAT.format(selectedDate[0]));

                        // Verify day of week matches
                        validateSelectedDate(selectedDate[0], yogaClass.getDayOfWeek());
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Show dialog
        new AlertDialog.Builder(this)
                .setTitle("Edit Class Instance")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Validate input
                    String teacher = etTeacher.getText().toString().trim();
                    if (teacher.isEmpty()) {
                        Toast.makeText(this, "Teacher name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Handle day of week mismatch
                    if (!isDayOfWeekMatch(selectedDate[0], yogaClass.getDayOfWeek())) {
                        new AlertDialog.Builder(this)
                                .setTitle("Date Mismatch")
                                .setMessage("The selected date (" + DATE_FORMAT.format(selectedDate[0]) +
                                        ") is not a " + yogaClass.getDayOfWeek() + ". Do you want to continue anyway?")
                                .setPositiveButton("Yes", (dialog2, which2) -> {
                                    // Update instance
                                    updateClassInstance(instance, selectedDate[0], teacher,
                                            etComments.getText().toString().trim());
                                })
                                .setNegativeButton("No", null)
                                .show();
                    } else {
                        // Update instance
                        updateClassInstance(instance, selectedDate[0], teacher,
                                etComments.getText().toString().trim());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateClassInstance(ClassInstance instance, Date date, String teacher, String comments) {
        instance.setDate(date);
        instance.setTeacher(teacher);
        instance.setComments(comments);

        int rowsAffected = databaseHelper.updateClassInstance(instance);
        if (rowsAffected > 0) {
            Toast.makeText(this, "Class instance updated", Toast.LENGTH_SHORT).show();
            loadClassInstances();  // Refresh list
        } else {
            Toast.makeText(this, "Failed to update class instance", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(ClassInstance instance) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Instance")
                .setMessage("Are you sure you want to delete this class instance?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteClassInstance(instance.getId());
                    Toast.makeText(this, "Class instance deleted", Toast.LENGTH_SHORT).show();
                    loadClassInstances();  // Refresh list
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Adapter for class instances
    private class ClassInstanceAdapter extends ArrayAdapter<ClassInstance> {
        public ClassInstanceAdapter() {
            super(ClassInstanceActivity.this, R.layout.item_class_instance, classInstances);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_class_instance, parent, false);
            }

            ClassInstance instance = getItem(position);

            TextView tvDate = convertView.findViewById(R.id.tvInstanceDate);
            TextView tvTeacher = convertView.findViewById(R.id.tvInstanceTeacher);
            TextView tvComments = convertView.findViewById(R.id.tvInstanceComments);

            if (instance != null) {
                tvDate.setText(DATE_FORMAT.format(instance.getDate()));
                tvTeacher.setText("Teacher: " + instance.getTeacher());

                if (instance.getComments() != null && !instance.getComments().isEmpty()) {
                    tvComments.setText(instance.getComments());
                    tvComments.setVisibility(View.VISIBLE);
                } else {
                    tvComments.setVisibility(View.GONE);
                }
            }

            return convertView;
        }
    }
}