package com.serkantken.ametist.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.serkantken.ametist.fragments.PictureFragment;
import com.serkantken.ametist.models.PhotoModel;
import com.serkantken.ametist.utilities.PhotoListener;

import java.util.ArrayList;

public class PicturesAdapter extends FragmentStateAdapter
{
    private Context context;
    ArrayList<PhotoModel> pictureList;
    PhotoListener listener;

    public PicturesAdapter(@NonNull FragmentActivity fragmentActivity, Context context, ArrayList<PhotoModel> pictureList, PhotoListener photoListener) {
        super(fragmentActivity);
        this.context = context;
        this.pictureList = pictureList;
        listener = photoListener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new PictureFragment(context, pictureList.get(position).getLink());
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }
}
