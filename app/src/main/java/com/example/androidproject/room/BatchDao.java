package com.example.androidproject.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * NOTE: add this DAO's interface to your existing AppDatabase (or whatever
 * your @Database class is called) alongside CourseDao, e.g.:
 *
 *   @Database(entities = {CourseEntity.class, BatchEntity.class}, version = <bump this>)
 *   public abstract class AppDatabase extends RoomDatabase {
 *       public abstract CourseDao courseDao();
 *       public abstract BatchDao batchDao();
 *   }
 *
 * Bumping the version will wipe local data unless you add a migration —
 * fine for a cache table, but flagging it since I can't see your current
 * AppDatabase to do this for you.
 */
@Dao
public interface BatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<BatchEntity> batches);

    @Query("SELECT * FROM batches WHERE courseId = :courseId ORDER BY batchName ASC")
    List<BatchEntity> getByCourse(int courseId);

    @Query("DELETE FROM batches WHERE courseId = :courseId")
    void deleteByCourse(int courseId);

    @Delete
    void delete(BatchEntity batch);
}