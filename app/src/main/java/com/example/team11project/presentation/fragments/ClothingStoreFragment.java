package com.example.team11project.presentation.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.team11project.R;
import com.example.team11project.presentation.adapters.ClothingAdapter;
import com.example.team11project.presentation.viewmodel.StoreViewModel;

import java.util.ArrayList;

public class ClothingStoreFragment extends Fragment {

    private StoreViewModel storeViewModel;
    private ClothingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_list, container, false);

        String userId = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("userId", null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ClothingAdapter(new ArrayList<>(), clothing -> {
            storeViewModel.buyClothing(userId, clothing);
            Toast.makeText(getContext(), "Kupljena: " + clothing.getName(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        storeViewModel = new ViewModelProvider(requireActivity()).get(StoreViewModel.class);
        storeViewModel.getClothing().observe(getViewLifecycleOwner(), adapter::updateData);

        storeViewModel.loadEquipment();

        return view;
    }
}