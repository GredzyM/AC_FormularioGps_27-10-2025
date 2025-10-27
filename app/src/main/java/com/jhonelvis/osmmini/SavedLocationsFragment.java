package com.jhonelvis.osmmini;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.jhonelvis.osmmini.data.AppDatabase;
import com.jhonelvis.osmmini.data.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedLocationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private PlaceAdapter adapter;
    private List<Place> placesList;
    private List<Place> allPlacesList;
    private AppDatabase database;
    private PlaceViewModel viewModel;
    private TextInputEditText searchBox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_locations, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(PlaceViewModel.class);
        database = AppDatabase.getDatabase(requireContext());
        
        placesList = new ArrayList<>();
        allPlacesList = new ArrayList<>();
        adapter = new PlaceAdapter(placesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        // Search box
        searchBox = view.findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlaces(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Load places from database
        loadPlaces();
        
        // Back button
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    private void loadPlaces() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Place> places = database.placeDao().getAll();
                
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        allPlacesList.clear();
                        allPlacesList.addAll(places);
                        placesList.clear();
                        placesList.addAll(places);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void filterPlaces(String searchText) {
        placesList.clear();
        
        if (searchText.isEmpty()) {
            placesList.addAll(allPlacesList);
        } else {
            String lowerSearch = searchText.toLowerCase();
            for (Place place : allPlacesList) {
                if (place.name.toLowerCase().contains(lowerSearch) ||
                    place.description.toLowerCase().contains(lowerSearch)) {
                    placesList.add(place);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
    }

    private class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
        private List<Place> places;

        public PlaceAdapter(List<Place> places) {
            this.places = places;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_place, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Place place = places.get(position);
            holder.bind(place);
        }

        @Override
        public int getItemCount() {
            return places.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textName;
            private TextView textDescription;
            private TextView textCoordinates;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.textName);
                textDescription = itemView.findViewById(R.id.textDescription);
                textCoordinates = itemView.findViewById(R.id.textCoordinates);
                
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Place place = places.get(getAdapterPosition());
                        viewModel.notifyLocationSelected(place);
                        // Close fragment and return to map
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }
                });
            }

            public void bind(Place place) {
                textName.setText(place.name);
                textDescription.setText(place.description.isEmpty() ? "Sin descripción" : place.description);
                textCoordinates.setText(String.format("Lat: %.6f, Lon: %.6f", place.lat, place.lon));
            }
        }
    }
}

