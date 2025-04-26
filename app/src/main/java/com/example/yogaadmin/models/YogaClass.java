package com.example.yogaadmin.models;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class YogaClass implements Serializable {
    // Required fields
    private long id; // Database ID
    private String dayOfWeek;
    private String courseTime;
    private int capacity;
    private int duration; // in minutes
    private double pricePerClass;
    private String classType;
    private String teacher;

    // Optional fields
    private String description;

    // Additional creative fields
    private String equipmentNeeded;
    private String difficultyLevel;

    // List of class instances
    private List<ClassInstance> classInstances;

    // Default constructor required for Firebase
    public YogaClass() {
        this.classInstances = new ArrayList<>();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTeacher() {
        return this.teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(String courseTime) {
        this.courseTime = courseTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getPricePerClass() {
        return pricePerClass;
    }

    public void setPricePerClass(double pricePerClass) {
        this.pricePerClass = pricePerClass;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEquipmentNeeded() {
        return equipmentNeeded;
    }

    public void setEquipmentNeeded(String equipmentNeeded) {
        this.equipmentNeeded = equipmentNeeded;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public List<ClassInstance> getClassInstances() {
        return classInstances;
    }

    public void setClassInstances(List<ClassInstance> classInstances) {
        this.classInstances = classInstances;
    }

    public void addClassInstance(ClassInstance instance) {
        if (this.classInstances == null) {
            this.classInstances = new ArrayList<>();
        }
        this.classInstances.add(instance);
    }

    // Validation method
    public boolean isValid() {
        return dayOfWeek != null && !dayOfWeek.isEmpty() &&
                courseTime != null && !courseTime.isEmpty() &&
                capacity > 0 &&
                duration > 0 &&
                pricePerClass >= 0 &&
                classType != null && !classType.isEmpty();
    }

    @Override
    public String toString() {
        return "YogaClass{" +
                "dayOfWeek='" + dayOfWeek + '\'' +
                ", courseTime='" + courseTime + '\'' +
                ", classType='" + classType + '\'' +
                ", teacher='" + teacher + '\'' +
                ", capacity=" + capacity +
                '}';
    }
}