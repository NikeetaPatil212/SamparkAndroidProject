package com.example.androidproject.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BatchEntity> batches);

    @Query("SELECT * FROM batches WHERE courseId = :courseId")
    List<BatchEntity> getByCourse(int courseId);

    @Query("DELETE FROM batches")
    void clearAll();
}