package com.example.yogaadmin.models;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class ClassInstance implements Serializable {
    private long id;
    private long yogaClassId; // Foreign key to YogaClass
    private Date date;
    private String teacher;
    private String comments;

    // Default constructor required for Firebase
    public ClassInstance() {
    }

    // Constructor with parameters
    public ClassInstance(long yogaClassId, Date date, String teacher, String comments) {
        this.yogaClassId = yogaClassId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getYogaClassId() {
        return yogaClassId;
    }

    public void setYogaClassId(long yogaClassId) {
        this.yogaClassId = yogaClassId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}