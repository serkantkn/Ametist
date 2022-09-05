package com.serkantken.ametist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.os.Bundle;

import com.serkantken.ametist.databinding.ActivityOnboardingBinding;
import com.serkantken.ametist.fragments.OnboardingFragments;

public class OnboardingActivity extends AppCompatActivity
{
    ActivityOnboardingBinding binding;

    private static final int NUM_PAGES = 3;
    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        binding.pager.setAdapter(pagerAdapter);
    }

    private static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
    {

        public ScreenSlidePagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position)
            {
                case 0:
                    return new OnboardingFragments.OnboardingFragment1();
                case 1:
                    return new OnboardingFragments.OnboardingFragment2();
                case 2:
                    return new OnboardingFragments.OnboardingFragment3();
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}