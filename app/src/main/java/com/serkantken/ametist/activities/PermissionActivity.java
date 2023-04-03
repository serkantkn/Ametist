package com.serkantken.ametist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.serkantken.ametist.databinding.ActivityPermissionBinding;
import com.serkantken.ametist.utilities.Utilities;

public class PermissionActivity extends AppCompatActivity {
    private ActivityPermissionBinding binding;
    private Utilities utilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        utilities = new Utilities(PermissionActivity.this, PermissionActivity.this);

        binding.getRoot().setPadding(utilities.convertDpToPixel(16), utilities.getStatusBarHeight()+utilities.convertDpToPixel(10), 0, 0);

        checkPermissions();

        binding.notificationPermissionCheck.setOnClickListener(view -> {
            boolean sdk = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2;
            if (!sdk)
            {
                getNotificationPermission();
            }
        });
        binding.animNotification.setOnClickListener(view -> {
            boolean sdk = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2;
            if (!sdk)
            {
                getNotificationPermission();
            }
        });

        binding.animGallery.setOnClickListener(view -> getPermissions());
        binding.galleryPermissionCheck.setOnClickListener(view -> getPermissions());

        binding.animLocation.setOnClickListener(view -> getLocationPermission());
        binding.locationPermissionCheck.setOnClickListener(view -> getLocationPermission());

        binding.skip.setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void checkPermissions()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            binding.locationPermissionCheck.playAnimation();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            binding.galleryPermissionCheck.playAnimation();
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
        {
            binding.notificationPermissionCheck.playAnimation();
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        {
            binding.notificationPermissionCheck.playAnimation();
        }
    }

    private void getPermissions()
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void getLocationPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    private void getNotificationPermission()
    {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Konum izini alındı", Toast.LENGTH_SHORT).show();
                checkPermissions();
            }
            else
            {
                Toast.makeText(this, "İzin reddedildi", Toast.LENGTH_SHORT).show();
                checkPermissions();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }
}