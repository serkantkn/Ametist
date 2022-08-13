package com.serkantken.ametist.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.serkantken.ametist.activities.LoginActivity;
import com.serkantken.ametist.databinding.FragmentOnboarding1Binding;
import com.serkantken.ametist.databinding.FragmentOnboarding2Binding;
import com.serkantken.ametist.databinding.FragmentOnboarding3Binding;

public class OnboardingFragments extends Fragment
{
    public static class OnboardingFragment1 extends Fragment {

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FragmentOnboarding1Binding binding = FragmentOnboarding1Binding.inflate(getLayoutInflater());

            binding.skip.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                requireActivity().finish();
            });
            return binding.getRoot();
        }
    }

    public static class OnboardingFragment2 extends Fragment {

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FragmentOnboarding2Binding binding = FragmentOnboarding2Binding.inflate(getLayoutInflater());

            binding.skip.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                requireActivity().finish();
            });
            return binding.getRoot();
        }
    }

    public static class OnboardingFragment3 extends Fragment {

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FragmentOnboarding3Binding binding = FragmentOnboarding3Binding.inflate(getLayoutInflater());

            binding.skip.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                requireActivity().finish();
            });

            return binding.getRoot();
        }
    }
}
