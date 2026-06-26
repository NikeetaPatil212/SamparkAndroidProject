package com.example.androidproject.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CourseEntity> courses);

    @Query("SELECT * FROM courses")
    List<CourseEntity> getAll();

    @Query("DELETE FROM courses")
    void clearAll();
}