package com.serkantken.ametist.activities;

import android.os.Bundle;
import android.transition.Fade;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.serkantken.ametist.R;
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

        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(decor.findViewById(R.id.toolbar), true);
        fade.excludeTarget(decor.findViewById(R.id.container), true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);

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