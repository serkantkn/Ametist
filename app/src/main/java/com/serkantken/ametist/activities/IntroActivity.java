package com.serkantken.ametist.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.serkantken.ametist.databinding.ActivityIntroBinding;
import com.serkantken.ametist.fragments.OnboardingFragments;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;

import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity
{
    private ActivityIntroBinding binding;
    private FirebaseAuth auth;
    Timer timer;

    private static final int NUM_PAGES = 3;
    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null)
        {
            pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            binding.pager.setAdapter(pagerAdapter);

            binding.imgLogo.animate().translationY(-1600).setDuration(1000).setStartDelay(4000);
            binding.appName.animate().translationY(-1600).setDuration(1000).setStartDelay(4000);
            binding.introBG.animate().translationY(-2600).setDuration(1000).setStartDelay(4000);
        }
        else
        {
            binding.imgLogo.animate().translationY(-1600).setDuration(1000).setStartDelay(3000);
            binding.appName.animate().translationY(-1600).setDuration(1000).setStartDelay(3000);
            binding.introBG.animate().translationY(-2600).setDuration(1000).setStartDelay(3000);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startActivity(new Intent(IntroActivity.this, MainActivity.class));
                    finish();
                }
            }, 2000);
        }
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