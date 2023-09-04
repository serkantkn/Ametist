package com.serkantken.ametist.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.serkantken.ametist.databinding.ActivityLoginBinding;
import com.serkantken.ametist.utilities.Utilities;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.serkantken.ametist.databinding.ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}