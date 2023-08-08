package com.serkantken.ametist.activities;

import android.os.Bundle;
import android.transition.Fade;
import android.view.View;

import com.bumptech.glide.Glide;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.ActivityFullProfilePhotoBinding;

import java.util.Objects;

public class FullProfilePhotoActivity extends BaseActivity
{
    ActivityFullProfilePhotoBinding binding;

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

        String pictureUrl = getIntent().getStringExtra("pictureUrl");

        if (!Objects.isNull(pictureUrl))
        {
            Glide.with(this).load(pictureUrl).into(binding.imageView);
            binding.imageView.setMaximumScale(5);
            binding.imageView.setMediumScale(3);
            binding.imageView.setMinimumScale(1);
        }
    }
}