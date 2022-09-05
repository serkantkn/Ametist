package com.serkantken.ametist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.serkantken.ametist.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null)
        {
            /*
            binding.imgLogo.animate().translationY(-1600).setDuration(1000).setStartDelay(4000);
            binding.appName.animate().translationY(-1600).setDuration(1000).setStartDelay(4000);
            binding.introBG.animate().translationY(-2600).setDuration(1000).setStartDelay(4000);*/
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startActivity(new Intent(IntroActivity.this, OnboardingActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }, 3000);
        }
        else
        {
            /*
            binding.imgLogo.animate().translationY(-1600).setDuration(1000).setStartDelay(3000);
            binding.appName.animate().translationY(-1600).setDuration(1000).setStartDelay(3000);
            binding.introBG.animate().translationY(-2600).setDuration(1000).setStartDelay(3000);*/
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startActivity(new Intent(IntroActivity.this, MainActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }, 2000);
        }
    }
}