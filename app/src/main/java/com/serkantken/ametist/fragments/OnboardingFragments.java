package com.serkantken.ametist.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.serkantken.ametist.activities.LoginActivity;
import com.serkantken.ametist.databinding.FragmentOnboarding1Binding;
import com.serkantken.ametist.databinding.FragmentOnboarding2Binding;
import com.serkantken.ametist.databinding.FragmentOnboarding3Binding;
import com.serkantken.ametist.utilities.Utilities;

public class OnboardingFragments extends Fragment
{
    public static class OnboardingFragment1 extends Fragment {
        Utilities utilities;
        Context context;
        Activity activity;

        public OnboardingFragment1(Context context, Activity activity) {
            this.context = context;
            this.activity = activity;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FragmentOnboarding1Binding binding = FragmentOnboarding1Binding.inflate(getLayoutInflater());
            utilities = new Utilities(context, activity);
            binding.skip.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                requireActivity().finish();
            });

            utilities.setMargins(binding.appName, utilities.convertDpToPixel(16), utilities.getStatusBarHeight()+utilities.convertDpToPixel(10), 0, 0);

            return binding.getRoot();
        }
    }

    public static class OnboardingFragment2 extends Fragment {
        Utilities utilities ;
        Context context;
        Activity activity;

        public OnboardingFragment2(Context context, Activity activity) {
            this.context = context;
            this.activity = activity;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FragmentOnboarding2Binding binding = FragmentOnboarding2Binding.inflate(getLayoutInflater());
            utilities = new Utilities(context, activity);
            binding.skip.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                requireActivity().finish();
            });

            utilities.setMargins(binding.appName, utilities.convertDpToPixel(16), utilities.getStatusBarHeight()+utilities.convertDpToPixel(10), 0, 0);

            return binding.getRoot();
        }
    }

    public static class OnboardingFragment3 extends Fragment {
        Utilities utilities;
        Context context;
        Activity activity;

        public OnboardingFragment3(Context context, Activity activity) {
            this.context = context;
            this.activity = activity;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            FragmentOnboarding3Binding binding = FragmentOnboarding3Binding.inflate(getLayoutInflater());
            utilities = new Utilities(context, activity);
            binding.skip.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                requireActivity().finish();
            });

            utilities.setMargins(binding.appName, utilities.convertDpToPixel(16), utilities.getStatusBarHeight()+utilities.convertDpToPixel(10), 0, 0);

            return binding.getRoot();
        }
    }
}
