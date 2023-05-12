package com.serkantken.ametist.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.serkantken.ametist.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.serkantken.ametist.databinding.ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}