package com.jhonelvis.osmmini.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "places")
public class Place {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;
    public double lat;
    public double lon;

    public Place(String name, String description, double lat, double lon) {
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lon = lon;
    }
}

