package com.serkantken.ametist.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.serkantken.ametist.R;
import com.serkantken.ametist.fragments.ChatListFragment;
import com.serkantken.ametist.fragments.DashboardFragment;
import com.serkantken.ametist.fragments.DiscoverFragment;
import com.serkantken.ametist.fragments.NotificationFragment;

public class MainAdapter extends FragmentStateAdapter
{
    private Context context;
    int totalTabs = 4;

    public MainAdapter(@NonNull FragmentActivity fm, Context context)
    {
        super(fm);
        this.context = context;
    }

    @Override
    public int getItemCount()
    {
        return totalTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        switch (position)
        {
            case 0:
                return new DashboardFragment();
            case 1:
                return new DiscoverFragment();
            case 2:
                return new ChatListFragment();
            case 3:
                return new NotificationFragment();
            default:
                throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    public int getTabIcon(int position)
    {
        switch (position)
        {
            case 0:
                return R.drawable.ic_home;
            case 1:
                return R.drawable.ic_group;
            case 2:
                return R.drawable.ic_message;
            case 3:
                return R.drawable.ic_notifications;
            default:
                throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return context.getString(R.string.app_name);
            case 1:
                return context.getString(R.string.discover);
            case 2:
                return context.getString(R.string.messages);
            case 3:
                return context.getString(R.string.notifications);
            default:
                throw new IllegalStateException("Unexpected position: " + position);
        }
    }
}
