package com.serkantken.ametist.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.serkantken.ametist.fragments.DashboardFragment;
import com.serkantken.ametist.fragments.DiscoverFragment;
import com.serkantken.ametist.fragments.NotificationFragment;
import com.serkantken.ametist.fragments.PictureFragments;

import java.util.ArrayList;

public class PicturesAdapter extends FragmentPagerAdapter
{
    private Context context;
    int totalTabs;
    ArrayList<String> pictureUris;

    public PicturesAdapter(@NonNull FragmentManager fm, Context context, int totalTabs, ArrayList<String> pictureUris)
    {
        super(fm);
        this.context = context;
        this.totalTabs = totalTabs;
        this.pictureUris = pictureUris;
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
                return new PictureFragments.FirstPicFragment(pictureUris.get(position));
            //case 1:
            //return new PictureFragments.SecondPicFragment(pictureUris.get(position));
            //case 2:
            //return new PictureFragments.ThirdPicFragment(pictureUris.get(position));
            //case 3:
            //return new PictureFragments.FourthPicFragment(pictureUris.get(position));
        }
        return null;
    }
}
