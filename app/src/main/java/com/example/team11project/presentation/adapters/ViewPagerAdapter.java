package com.example.team11project.presentation.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.team11project.presentation.fragments.CalendarFragment;
import com.example.team11project.presentation.fragments.TaskListFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Vraća odgovarajući fragment na osnovu pozicije taba
        switch (position) {
            case 0:
                return new TaskListFragment();
            case 1:
                return new CalendarFragment();
            default:
                return new TaskListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
