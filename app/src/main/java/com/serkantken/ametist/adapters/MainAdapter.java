package com.serkantken.ametist.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.serkantken.ametist.fragments.DashboardFragment;
import com.serkantken.ametist.fragments.DiscoverFragment;
import com.serkantken.ametist.fragments.NotificationFragment;

public class MainAdapter extends FragmentPagerAdapter
{
    private Context context;
    int totalTabs;

    public MainAdapter(@NonNull FragmentManager fm, Context context, int totalTabs)
    {
        super(fm);
        this.context = context;
        this.totalTabs = totalTabs;
    }

    @Override
    public int getCount()
    {
        return totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new DashboardFragment();
            case 1:
                return new DiscoverFragment();
            case 2:
                return new NotificationFragment();
        }
        return null;
    }
}
