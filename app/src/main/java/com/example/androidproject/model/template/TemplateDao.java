package com.example.androidproject.model.template;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TemplateEntity> templates);

    @Query("SELECT * FROM templates WHERE isActive = 1")
    List<TemplateEntity> getAllActiveTemplates();

    // ── Most used method — called from every screen ───────────────────────────
    @Query("SELECT * FROM templates WHERE category = :category AND isActive = 1 LIMIT 1")
    TemplateEntity getTemplateByCategory(String category);

    @Query("SELECT MIN(cachedAt) FROM templates")
    long getOldestCacheTimestamp();

    @Query("SELECT COUNT(*) FROM templates")
    int getCount();

    @Query("DELETE FROM templates")
    void clearAll();
}