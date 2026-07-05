package com.example.androidproject.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses")
public class CourseEntity {
    @PrimaryKey
    public int    courseId;
    public String courseName;
    public double fees;
    public String scheme;       // "Regular" or "Monthly"
    public int    certificate;  // 1 = Yes, 0 = No
    public int    duration;
}