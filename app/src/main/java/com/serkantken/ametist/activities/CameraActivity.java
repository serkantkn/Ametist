package com.serkantken.ametist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.serkantken.ametist.databinding.ActivityCameraBinding;

public class CameraActivity extends AppCompatActivity {
    private ActivityCameraBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}