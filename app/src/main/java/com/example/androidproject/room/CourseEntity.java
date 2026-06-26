package com.example.androidproject.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses")
public class CourseEntity {
    @PrimaryKey
    public int courseId;
    public String courseName;
}