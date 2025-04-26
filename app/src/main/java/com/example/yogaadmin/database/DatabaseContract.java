package com.example.yogaadmin.database;

import android.provider.BaseColumns;

public final class DatabaseContract {
    // Private constructor to prevent instantiation
    private DatabaseContract() {}

    // Inner class for Yoga Class table
    public static class YogaClassEntry implements BaseColumns {
        // Table name
        public static final String TABLE_NAME = "yoga_classes";

        // Columns
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DAY_OF_WEEK = "day_of_week";
        public static final String COLUMN_COURSE_TIME = "course_time";
        public static final String COLUMN_CAPACITY = "capacity";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_CLASS_TYPE = "class_type";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_EQUIPMENT = "equipment";
        public static final String COLUMN_TEACHER_NAME = "teacher_name";
    }
    // ClassInstance table contents
    public static class ClassInstanceEntry implements BaseColumns {
        public static final String TABLE_NAME = "class_instances";
        public static final String COLUMN_YOGA_CLASS_ID = "yoga_class_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TEACHER = "teacher";
        public static final String COLUMN_COMMENTS = "comments";
    }
}