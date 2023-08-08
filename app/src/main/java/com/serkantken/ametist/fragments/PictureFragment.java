package com.serkantken.ametist.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.serkantken.ametist.databinding.FragmentFirstPicBinding;

public class PictureFragment extends Fragment
{
    FragmentFirstPicBinding binding;
    Context context;
    String uri;

    public PictureFragment(Context context, String pictureUri)
    {
        this.context = context;
        this.uri = pictureUri;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstPicBinding.inflate(getLayoutInflater());
        Glide.with(context).load(uri).into(binding.ivFirstPic);
        return binding.getRoot();
    }
}
