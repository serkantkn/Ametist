package com.serkantken.ametist.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.serkantken.ametist.adapters.PicturesAdapter;
import com.serkantken.ametist.databinding.ActivityFullProfilePhotoBinding;

import java.util.ArrayList;

public class FullProfilePhotoActivity extends BaseActivity
{
    ActivityFullProfilePhotoBinding binding;
    ArrayList<String> pictureUris;
    PicturesAdapter adapter;
    FirebaseFirestore database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullProfilePhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        pictureUris = new ArrayList<>();
        String url = getIntent().getStringExtra("pictureUrl");
        pictureUris.add(url);

        adapter = new PicturesAdapter(getSupportFragmentManager(), getApplicationContext(), pictureUris.size(), pictureUris);
        binding.profileImageSlider.setAdapter(adapter);

        binding.buttonBack.setOnClickListener(view -> onBackPressed());
    }
}