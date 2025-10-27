package com.jhonelvis.osmmini;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jhonelvis.osmmini.data.Place;

public class PlaceViewModel extends ViewModel {
    private final MutableLiveData<LatLon> selectedLatLon = new MutableLiveData<>();
    private final MutableLiveData<Place> newPlaceSaved = new MutableLiveData<>();

    public void setLatLon(double lat, double lon) {
        selectedLatLon.setValue(new LatLon(lat, lon));
    }

    public LiveData<LatLon> getSelectedLatLon() {
        return selectedLatLon;
    }

    public void notifyPlaceSaved(Place place) {
        newPlaceSaved.setValue(place);
    }

    public LiveData<Place> getNewPlaceSaved() {
        return newPlaceSaved;
    }

    public void notifyLocationSelected(Place place) {
        // Notify when a saved location is selected from list
        // This will center the map on that location
        setLatLon(place.lat, place.lon);
    }

    public static class LatLon {
        public final double lat;
        public final double lon;

        public LatLon(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}

