package com.example.team11project.presentation.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.team11project.presentation.fragments.ClothingStoreFragment;
import com.example.team11project.presentation.fragments.PotionStoreFragment;

public class StorePagerAdapter extends FragmentStateAdapter {

    public StorePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PotionStoreFragment();
        } else {
            return new ClothingStoreFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
