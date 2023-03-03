package com.valterc.ki2.activities.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.valterc.ki2.fragments.devices.list.ListDevicesFragment;
import com.valterc.ki2.fragments.settings.SettingsHostFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0: return new ListDevicesFragment();
            case 1: return new SettingsHostFragment();
        }

        throw new RuntimeException("Unexpected fragment position");
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}
