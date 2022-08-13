package com.serkantken.ametist.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.FragmentFirstPicBinding;
import com.serkantken.ametist.databinding.FragmentFourthPicBinding;
import com.serkantken.ametist.databinding.FragmentSecondPicBinding;
import com.serkantken.ametist.databinding.FragmentThirdPicBinding;

public class PictureFragments extends Fragment
{
    public static class FirstPicFragment extends Fragment
    {
        FragmentFirstPicBinding binding;
        String uri;

        public FirstPicFragment(String pictureUri)
        {
            this.uri = pictureUri;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            binding = FragmentFirstPicBinding.inflate(getLayoutInflater());
            Glide.with(requireContext()).load(uri).into(binding.ivFirstPic);
            return binding.getRoot();
        }
    }

    public static class SecondPicFragment extends Fragment
    {
        FragmentSecondPicBinding binding;
        String uri;

        public SecondPicFragment(String pictureUri)
        {
            this.uri = pictureUri;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            binding = FragmentSecondPicBinding.inflate(getLayoutInflater());
            Glide.with(requireContext()).load(uri).into(binding.ivSecondPic);
            return inflater.inflate(R.layout.fragment_second_pic, container, false);
        }
    }

    public static class ThirdPicFragment extends Fragment
    {
        FragmentThirdPicBinding binding;
        String uri;

        public ThirdPicFragment(String pictureUri)
        {
            this.uri = pictureUri;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            binding = FragmentThirdPicBinding.inflate(getLayoutInflater());
            Glide.with(requireContext()).load(uri).into(binding.ivThirdPic);
            return binding.getRoot();
        }
    }

    public static class FourthPicFragment extends Fragment
    {
        FragmentFourthPicBinding binding;
        String uri;

        public FourthPicFragment(String pictureUri)
        {
            this.uri = pictureUri;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            binding = FragmentFourthPicBinding.inflate(getLayoutInflater());
            Glide.with(requireContext()).load(uri).into(binding.ivFourthPic);
            return binding.getRoot();
        }
    }
}
