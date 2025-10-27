package com.jhonelvis.osmmini.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Place place);
    
    @Query("SELECT * FROM places ORDER BY id DESC")
    List<Place> getAll();
}

