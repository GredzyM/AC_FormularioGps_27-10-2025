package com.jhonelvis.osmmini;

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
import com.jhonelvis.osmmini.databinding.FragmentFormBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FormFragment extends Fragment {
    private FragmentFormBinding binding;
    private PlaceViewModel viewModel;
    private AppDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(PlaceViewModel.class);
        database = AppDatabase.getDatabase(requireContext());
        
        // Observe ViewModel for coordinates
        LiveData<PlaceViewModel.LatLon> latLonLiveData = viewModel.getSelectedLatLon();
        latLonLiveData.observe(getViewLifecycleOwner(), new Observer<PlaceViewModel.LatLon>() {
            @Override
            public void onChanged(PlaceViewModel.LatLon latLon) {
                if (latLon != null) {
                    binding.etLatitude.setText(String.format("%.6f", latLon.lat));
                    binding.etLongitude.setText(String.format("%.6f", latLon.lon));
                }
            }
        });
        
        // Save button
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlace();
            }
        });
    }

    private void savePlace() {
        String name = binding.etName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        
        // Validate
        if (name.isEmpty()) {
            binding.etName.setError("El nombre es obligatorio");
            return;
        }
        
        try {
            double lat = Double.parseDouble(binding.etLatitude.getText().toString());
            double lon = Double.parseDouble(binding.etLongitude.getText().toString());
            
            Place place = new Place(name, description, lat, lon);
            
            // Insert in background thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    database.placeDao().insert(place);
                    
                    // Show toast and notify ViewModel on UI thread
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Guardado", Toast.LENGTH_SHORT).show();
                            // Notify ViewModel so MapFragment can add permanent marker
                            viewModel.notifyPlaceSaved(place);
                            // Clear form fields
                            binding.etName.setText("");
                            binding.etDescription.setText("");
                        }
                    });
                }
            });
            
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
        }
    }
}

