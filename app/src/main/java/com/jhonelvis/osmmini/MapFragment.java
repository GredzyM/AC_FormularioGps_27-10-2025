package com.jhonelvis.osmmini;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.jhonelvis.osmmini.data.AppDatabase;
import com.jhonelvis.osmmini.data.Place;
import com.jhonelvis.osmmini.databinding.FragmentMapBinding;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {
    private FragmentMapBinding binding;
    private MapView mapView;
    private Marker currentMarker; // Temporary marker for selection
    private java.util.List<Marker> permanentMarkers = new java.util.ArrayList<>(); // Saved places markers
    private PlaceViewModel viewModel;
    private AppDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(PlaceViewModel.class);
        mapView = binding.mapView;
        
        // Configure map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        
        // Center on Santa Cruz de la Sierra
        IMapController mapController = mapView.getController();
        GeoPoint santaCruz = new GeoPoint(-17.7833, -63.1821);
        mapController.setZoom(12.0);
        mapController.setCenter(santaCruz);
        
        // Add tap listener
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                putMarker(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        
        MapEventsOverlay overlayEvents = new MapEventsOverlay(receiver);
        mapView.getOverlays().add(0, overlayEvents);
        
        // Load database
        database = AppDatabase.getDatabase(requireContext());
        
        // Load saved places as permanent markers
        loadSavedPlaces();
        
        // Listen for newly saved places
        viewModel.getNewPlaceSaved().observe(getViewLifecycleOwner(), new Observer<Place>() {
            @Override
            public void onChanged(Place place) {
                if (place != null) {
                    addPermanentMarker(place);
                    Toast.makeText(requireContext(), "Marker agregado al mapa", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Listen for location selection from saved locations list
        final Observer<PlaceViewModel.LatLon> latLonObserver = new Observer<PlaceViewModel.LatLon>() {
            @Override
            public void onChanged(PlaceViewModel.LatLon latLon) {
                if (latLon != null && mapView != null) {
                    // Small delay to ensure fragment transition completes
                    mapView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            centerMapOnLocation(latLon.lat, latLon.lon);
                        }
                    }, 300);
                }
            }
        };
        viewModel.getSelectedLatLon().observeForever(latLonObserver);
        
        // FAB to open saved locations
        binding.fabLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SavedLocationsFragment fragment = new SavedLocationsFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void putMarker(GeoPoint point) {
        if (currentMarker != null) {
            mapView.getOverlays().remove(currentMarker);
        }
        
        currentMarker = new Marker(mapView);
        currentMarker.setPosition(point);
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        currentMarker.setTitle(point.getLatitude() + ", " + point.getLongitude());
        
        mapView.getOverlays().add(currentMarker);
        mapView.invalidate();
        
        // Update ViewModel
        viewModel.setLatLon(point.getLatitude(), point.getLongitude());
    }
    
    private void loadSavedPlaces() {
        // Load saved places in background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Place> places = database.placeDao().getAll();
                
                // Add markers on UI thread
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Place place : places) {
                            addPermanentMarker(place);
                        }
                    }
                });
            }
        });
    }
    
    private void addPermanentMarker(Place place) {
        Marker marker = new Marker(mapView);
        GeoPoint geoPoint = new GeoPoint(place.lat, place.lon);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(place.name);
        
        // Set beige color for permanent markers
        marker.getIcon().setTint(Color.parseColor("#D2B48C"));
        
        // Add to overlays and keep reference
        mapView.getOverlays().add(marker);
        permanentMarkers.add(marker);
        mapView.invalidate();
    }
    
    private void centerMapOnLocation(double lat, double lon) {
        IMapController mapController = mapView.getController();
        mapController.setCenter(new GeoPoint(lat, lon));
        mapController.zoomTo(15.0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
}

