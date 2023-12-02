package com.serkantken.ametist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.hawk.Hawk;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.ActivitySettingsBinding;
import com.serkantken.ametist.utilities.Constants;
import com.serkantken.ametist.utilities.Utilities;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class SettingsActivity extends BaseActivity
{
    private ActivitySettingsBinding binding;
    private Balloon balloon;
    private Utilities utilities;
    private FirebaseFirestore database;
    private FirebaseAuth auth;
    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        utilities = new Utilities(getApplicationContext(), this);
        Hawk.init(this).build();
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        documentReference = database.collection("Users").document(Objects.requireNonNull(auth.getUid()));

        binding.buttonBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.setPadding(0, utilities.getStatusBarHeight(), 0, 0);
        binding.scrollLayout.setPadding(0, utilities.getStatusBarHeight()+utilities.convertDpToPixel(70), 0, 0);

        utilities.blur(new BlurView[]{binding.toolbar}, 10f, false);

        if (Hawk.contains("online"))
            binding.switchShowOnline.setChecked((int) Hawk.get("online") == 1);

        if (Hawk.contains("lastSeen"))
            binding.switchShowLastSeen.setChecked((int) Hawk.get("lastSeen") == 1);

        if (Hawk.contains("blurUI"))
            binding.switchBlur.setChecked((int) Hawk.get("blurUI") == 1);

        binding.switchShowOnline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
            {
                Hawk.put("online", 1);
                documentReference.update("online", true);
            }
            else
            {
                Hawk.put("online", 0);
                documentReference.update("online", false);
                binding.switchShowLastSeen.setChecked(false);
            }
        });

        binding.switchShowLastSeen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
            {
                Hawk.put("lastSeen", 1);
                documentReference.update("lastSeen", new Date().getTime());
            }
            else
            {
                Hawk.put("lastSeen", 0);
                documentReference.update("lastSeen", FieldValue.delete());
            }
        });

        binding.switchBlur.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                Hawk.put("blurUI", 1);
            else
                Hawk.put("blurUI", 0);
        });

        binding.resetBalloons.setOnClickListener(this::resetBalloons);
        binding.logout.setOnClickListener(this::signOut);
    }

    private void resetBalloons(View view)
    {
        Hawk.put(Constants.IS_BALLOONS_SHOWED, false);
        showBalloon(getString(R.string.restart_to_see_changes), binding.resetBalloons, 3);
    }

    private void signOut(View view)
    {
        documentReference.update("token", FieldValue.delete()).addOnSuccessListener(unused -> {
            FirebaseAuth.getInstance().signOut();
            Hawk.deleteAll();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> Toast.makeText(this, getString(R.string.unable_sign_out), Toast.LENGTH_SHORT).show());
    }

    private void showBalloon(String message, View view, int position)
    {
        balloon = new Balloon.Builder(getApplicationContext())
                .setArrowSize(10)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setText(message)
                .setTextSize(15f)
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white))
                .setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.accent_blue_dark))
                .setArrowPosition(0.5f)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setPadding(12)
                .setCornerRadius(8f)
                .setBalloonAnimation(BalloonAnimation.ELASTIC)
                .build();
        switch (position)
        {
            case 1:
                balloon.showAlignTop(view);
                break;
            case 2:
                balloon.showAlignRight(view);
                break;
            case 3:
                balloon.showAlignBottom(view);
                break;
            case 4:
                balloon.showAlignLeft(view);
                break;
        }
    }
}