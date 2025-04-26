package com.example.yogaadmin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.yogaadmin.models.ClassInstance;
import com.example.yogaadmin.models.YogaClass;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "UniversalYoga.db";
    private static final int DATABASE_VERSION = 5; // Increased version number for schema change

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Yoga Class Table
        String CREATE_YOGA_CLASS_TABLE = "CREATE TABLE " + DatabaseContract.YogaClassEntry.TABLE_NAME + "("
                + DatabaseContract.YogaClassEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.YogaClassEntry.COLUMN_DAY_OF_WEEK + " TEXT NOT NULL, "
                + DatabaseContract.YogaClassEntry.COLUMN_COURSE_TIME + " TEXT NOT NULL, "
                + DatabaseContract.YogaClassEntry.COLUMN_CAPACITY + " INTEGER NOT NULL, "
                + DatabaseContract.YogaClassEntry.COLUMN_DURATION + " INTEGER NOT NULL, "
                + DatabaseContract.YogaClassEntry.COLUMN_PRICE + " REAL NOT NULL, "
                + DatabaseContract.YogaClassEntry.COLUMN_CLASS_TYPE + " TEXT NOT NULL, "
                + DatabaseContract.YogaClassEntry.COLUMN_DESCRIPTION + " TEXT, "
                + DatabaseContract.YogaClassEntry.COLUMN_EQUIPMENT + " TEXT, "
                + DatabaseContract.YogaClassEntry.COLUMN_TEACHER_NAME + " TEXT)";

        // Create Class Instance Table
        String CREATE_CLASS_INSTANCE_TABLE = "CREATE TABLE " + DatabaseContract.ClassInstanceEntry.TABLE_NAME + "("
                + DatabaseContract.ClassInstanceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DatabaseContract.ClassInstanceEntry.COLUMN_YOGA_CLASS_ID + " INTEGER NOT NULL, "
                + DatabaseContract.ClassInstanceEntry.COLUMN_DATE + " INTEGER NOT NULL, "
                + DatabaseContract.ClassInstanceEntry.COLUMN_TEACHER + " TEXT NOT NULL, "
                + DatabaseContract.ClassInstanceEntry.COLUMN_COMMENTS + " TEXT, "
                + "FOREIGN KEY(" + DatabaseContract.ClassInstanceEntry.COLUMN_YOGA_CLASS_ID + ") REFERENCES "
                + DatabaseContract.YogaClassEntry.TABLE_NAME + "(" + DatabaseContract.YogaClassEntry._ID + "))";

        db.execSQL(CREATE_YOGA_CLASS_TABLE);
        db.execSQL(CREATE_CLASS_INSTANCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables if exists
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ClassInstanceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.YogaClassEntry.TABLE_NAME);

        // Create new tables
        onCreate(db);
    }

    // Insert a new Yoga Class
    public long insertYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.YogaClassEntry.COLUMN_DAY_OF_WEEK, yogaClass.getDayOfWeek());
        values.put(DatabaseContract.YogaClassEntry.COLUMN_COURSE_TIME, yogaClass.getCourseTime());
        values.put(DatabaseContract.YogaClassEntry.COLUMN_CAPACITY, yogaClass.getCapacity());
        values.put(DatabaseContract.YogaClassEntry.COLUMN_DURATION, yogaClass.getDuration());
        values.put(DatabaseContract.YogaClassEntry.COLUMN_PRICE, yogaClass.getPricePerClass());
        values.put(DatabaseContract.YogaClassEntry.COLUMN_CLASS_TYPE, yogaClass.getClassType());
        values.put(DatabaseContract.YogaClassEntry.COLUMN_DESCRIPTION, yogaClass.getDescription());
        values.put(DatabaseContract.YogaClassEntry.COLUMN_EQUIPMENT, yogaClass.getEquipmentNeeded());

        // Explicitly set teacher name
        String teacherName = yogaClass.getTeacher();
        if (teacherName == null) {
            teacherName = "Unknown"; // Default value if null
        }
        values.put(DatabaseContract.YogaClassEntry.COLUMN_TEACHER_NAME, teacherName);

        long id = db.insert(DatabaseContract.YogaClassEntry.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // Get All Yoga Classes
    public List<YogaClass> getAllYogaClasses() {
        List<YogaClass> yogaClasses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseContract.YogaClassEntry.TABLE_NAME,
                    null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    YogaClass yogaClass = new YogaClass();

                    // Safely retrieve values with index checks
                    int idIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry._ID);
                    int dayIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DAY_OF_WEEK);
                    int timeIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_COURSE_TIME);
                    int capacityIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_CAPACITY);
                    int durationIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DURATION);
                    int priceIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_PRICE);
                    int typeIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_CLASS_TYPE);
                    int descIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DESCRIPTION);
                    int equipmentIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_EQUIPMENT);
                    int teacherIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_TEACHER_NAME);

                    // Set values only if index is valid
                    if (idIndex != -1) yogaClass.setId(cursor.getLong(idIndex));
                    if (dayIndex != -1) yogaClass.setDayOfWeek(cursor.getString(dayIndex));
                    if (timeIndex != -1) yogaClass.setCourseTime(cursor.getString(timeIndex));
                    if (capacityIndex != -1) yogaClass.setCapacity(cursor.getInt(capacityIndex));
                    if (durationIndex != -1) yogaClass.setDuration(cursor.getInt(durationIndex));
                    if (priceIndex != -1) yogaClass.setPricePerClass(cursor.getDouble(priceIndex));
                    if (typeIndex != -1) yogaClass.setClassType(cursor.getString(typeIndex));
                    if (descIndex != -1) yogaClass.setDescription(cursor.getString(descIndex));
                    if (equipmentIndex != -1) yogaClass.setEquipmentNeeded(cursor.getString(equipmentIndex));
                    if (teacherIndex != -1) {
                        String teacher = cursor.getString(teacherIndex);
                        yogaClass.setTeacher(teacher != null ? teacher : "Unknown");
                    }

                    yogaClasses.add(yogaClass);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving yoga classes", e);
        } finally {
            // Ensure cursor is closed
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return yogaClasses;
    }

    // Search Classes by Teacher Name
    public List<YogaClass> searchClassesByTeacher(String teacherName) {
        List<YogaClass> matchingClasses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Search for classes with matching teacher name
            cursor = db.query(
                    DatabaseContract.YogaClassEntry.TABLE_NAME,
                    null,
                    DatabaseContract.YogaClassEntry.COLUMN_TEACHER_NAME + " LIKE ?",
                    new String[]{"%" + teacherName + "%"},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    YogaClass yogaClass = new YogaClass();

                    // Safely retrieve values with index checks
                    int idIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry._ID);
                    int dayIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DAY_OF_WEEK);
                    int timeIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_COURSE_TIME);
                    int capacityIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_CAPACITY);
                    int durationIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DURATION);
                    int priceIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_PRICE);
                    int typeIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_CLASS_TYPE);
                    int descIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DESCRIPTION);
                    int equipmentIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_EQUIPMENT);
                    int teacherIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_TEACHER_NAME);

                    // Set values only if index is valid
                    if (idIndex != -1) yogaClass.setId(cursor.getLong(idIndex));
                    if (dayIndex != -1) yogaClass.setDayOfWeek(cursor.getString(dayIndex));
                    if (timeIndex != -1) yogaClass.setCourseTime(cursor.getString(timeIndex));
                    if (capacityIndex != -1) yogaClass.setCapacity(cursor.getInt(capacityIndex));
                    if (durationIndex != -1) yogaClass.setDuration(cursor.getInt(durationIndex));
                    if (priceIndex != -1) yogaClass.setPricePerClass(cursor.getDouble(priceIndex));
                    if (typeIndex != -1) yogaClass.setClassType(cursor.getString(typeIndex));
                    if (descIndex != -1) yogaClass.setDescription(cursor.getString(descIndex));
                    if (equipmentIndex != -1) yogaClass.setEquipmentNeeded(cursor.getString(equipmentIndex));
                    if (teacherIndex != -1) {
                        String teacher = cursor.getString(teacherIndex);
                        yogaClass.setTeacher(teacher != null ? teacher : "Unknown");
                    }

                    matchingClasses.add(yogaClass);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching classes by teacher", e);
        } finally {
            // Ensure cursor is closed
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return matchingClasses;
    }

    public YogaClass getYogaClassById(long id) {
        YogaClass yogaClass = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseContract.YogaClassEntry.TABLE_NAME,
                    null,
                    DatabaseContract.YogaClassEntry._ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                yogaClass = new YogaClass();

                // Safely retrieve values with index checks
                int idIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry._ID);
                int dayIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DAY_OF_WEEK);
                int timeIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_COURSE_TIME);
                int capacityIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_CAPACITY);
                int durationIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DURATION);
                int priceIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_PRICE);
                int typeIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_CLASS_TYPE);
                int descIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_DESCRIPTION);
                int equipmentIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_EQUIPMENT);
                int teacherIndex = cursor.getColumnIndex(DatabaseContract.YogaClassEntry.COLUMN_TEACHER_NAME);

                // Set values only if index is valid
                if (idIndex != -1) yogaClass.setId(cursor.getLong(idIndex));
                if (dayIndex != -1) yogaClass.setDayOfWeek(cursor.getString(dayIndex));
                if (timeIndex != -1) yogaClass.setCourseTime(cursor.getString(timeIndex));
                if (capacityIndex != -1) yogaClass.setCapacity(cursor.getInt(capacityIndex));
                if (durationIndex != -1) yogaClass.setDuration(cursor.getInt(durationIndex));
                if (priceIndex != -1) yogaClass.setPricePerClass(cursor.getDouble(priceIndex));
                if (typeIndex != -1) yogaClass.setClassType(cursor.getString(typeIndex));
                if (descIndex != -1 && !cursor.isNull(descIndex)) yogaClass.setDescription(cursor.getString(descIndex));
                if (equipmentIndex != -1 && !cursor.isNull(equipmentIndex)) yogaClass.setEquipmentNeeded(cursor.getString(equipmentIndex));
                if (teacherIndex != -1) {
                    String teacher = cursor.getString(teacherIndex);
                    yogaClass.setTeacher(teacher != null ? teacher : "Unknown");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving yoga class by ID: " + id, e);
        } finally {
            // Ensure cursor is closed
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return yogaClass;
    }

    // Delete a Specific Yoga Class
    // Delete a Specific Yoga Class
    public void deleteYogaClass(long id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(DatabaseContract.YogaClassEntry.TABLE_NAME,
                    DatabaseContract.YogaClassEntry._ID + " = ?",
                    new String[]{String.valueOf(id)});
            // Don't close the database here - let the system manage it
        } catch (Exception e) {
            Log.e(TAG, "Error deleting yoga class: " + e.getMessage(), e);
        }
    }

    // Reset Database (delete all records)
    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Delete class instances first due to foreign key constraints
            db.delete(DatabaseContract.ClassInstanceEntry.TABLE_NAME, null, null);
            // Then delete yoga classes
            db.delete(DatabaseContract.YogaClassEntry.TABLE_NAME, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Error resetting database", e);
        } finally {
            db.close();
        }
    }

    // Insert a new Class Instance
    public long insertClassInstance(ClassInstance instance) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ClassInstanceEntry.COLUMN_YOGA_CLASS_ID, instance.getYogaClassId());
        values.put(DatabaseContract.ClassInstanceEntry.COLUMN_DATE, instance.getDate().getTime()); // Store as milliseconds
        values.put(DatabaseContract.ClassInstanceEntry.COLUMN_TEACHER, instance.getTeacher());
        values.put(DatabaseContract.ClassInstanceEntry.COLUMN_COMMENTS, instance.getComments());

        long id = db.insert(DatabaseContract.ClassInstanceEntry.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // Get all instances for a specific yoga class
    public List<ClassInstance> getClassInstancesByYogaClassId(long yogaClassId) {
        List<ClassInstance> instances = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseContract.ClassInstanceEntry.TABLE_NAME,
                    null,
                    DatabaseContract.ClassInstanceEntry.COLUMN_YOGA_CLASS_ID + " = ?",
                    new String[]{String.valueOf(yogaClassId)},
                    null,
                    null,
                    DatabaseContract.ClassInstanceEntry.COLUMN_DATE + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ClassInstance instance = new ClassInstance();

                    int idIndex = cursor.getColumnIndex(DatabaseContract.ClassInstanceEntry._ID);
                    int yogaClassIdIndex = cursor.getColumnIndex(DatabaseContract.ClassInstanceEntry.COLUMN_YOGA_CLASS_ID);
                    int dateIndex = cursor.getColumnIndex(DatabaseContract.ClassInstanceEntry.COLUMN_DATE);
                    int teacherIndex = cursor.getColumnIndex(DatabaseContract.ClassInstanceEntry.COLUMN_TEACHER);
                    int commentsIndex = cursor.getColumnIndex(DatabaseContract.ClassInstanceEntry.COLUMN_COMMENTS);

                    if (idIndex != -1) instance.setId(cursor.getLong(idIndex));
                    if (yogaClassIdIndex != -1) instance.setYogaClassId(cursor.getLong(yogaClassIdIndex));
                    if (dateIndex != -1) {
                        long dateMillis = cursor.getLong(dateIndex);
                        instance.setDate(new Date(dateMillis));
                    }
                    if (teacherIndex != -1) instance.setTeacher(cursor.getString(teacherIndex));
                    if (commentsIndex != -1) instance.setComments(cursor.getString(commentsIndex));

                    instances.add(instance);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving class instances", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return instances;
    }

    // Update a class instance
    public int updateClassInstance(ClassInstance instance) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ClassInstanceEntry.COLUMN_YOGA_CLASS_ID, instance.getYogaClassId());
        values.put(DatabaseContract.ClassInstanceEntry.COLUMN_DATE, instance.getDate().getTime());
        values.put(DatabaseContract.ClassInstanceEntry.COLUMN_TEACHER, instance.getTeacher());
        values.put(DatabaseContract.ClassInstanceEntry.COLUMN_COMMENTS, instance.getComments());

        int rowsAffected = db.update(
                DatabaseContract.ClassInstanceEntry.TABLE_NAME,
                values,
                DatabaseContract.ClassInstanceEntry._ID + " = ?",
                new String[]{String.valueOf(instance.getId())}
        );

        db.close();
        return rowsAffected;
    }

    // Delete a class instance
    public void deleteClassInstance(long instanceId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.delete(
                    DatabaseContract.ClassInstanceEntry.TABLE_NAME,
                    DatabaseContract.ClassInstanceEntry._ID + " = ?",
                    new String[]{String.valueOf(instanceId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error deleting class instance", e);
        } finally {
            db.close();
        }
    }

    // Delete all class instances for a yoga class
    public void deleteClassInstancesByYogaClassId(long yogaClassId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.delete(
                    DatabaseContract.ClassInstanceEntry.TABLE_NAME,
                    DatabaseContract.ClassInstanceEntry.COLUMN_YOGA_CLASS_ID + " = ?",
                    new String[]{String.valueOf(yogaClassId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error deleting class instances for yoga class", e);
        } finally {
            db.close();
        }
    }
}