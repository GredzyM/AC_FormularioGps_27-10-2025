package com.jhonelvis.osmmini;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.jhonelvis.osmmini.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Create ViewModel
        new ViewModelProvider(this).get(PlaceViewModel.class);
        
        // Add fragments if this is first time
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            
            transaction.add(binding.containerForm.getId(), new FormFragment());
            transaction.add(binding.containerMap.getId(), new MapFragment());
            transaction.commit();
        }
    }
}

