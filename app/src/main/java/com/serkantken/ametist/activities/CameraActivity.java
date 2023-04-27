package com.serkantken.ametist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.ActivityCameraBinding;
import com.serkantken.ametist.utilities.CameraPreview;
import com.serkantken.ametist.utilities.Utilities;

public class CameraActivity extends AppCompatActivity {
    private ActivityCameraBinding binding;
    private Utilities utilities;
    private Camera mCamera;
    private CameraPreview mPreview;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        utilities = new Utilities(this, this);

        binding.toolbar.setPadding(0, utilities.getStatusBarHeight(), 0, 0);
        binding.shutterArea.setPadding(0, 0, 0, utilities.getNavigationBarHeight(Configuration.ORIENTATION_PORTRAIT));

        mCamera = getCameraInstance();

        mPreview = new CameraPreview(this, mCamera);
        binding.frameLayout.addView(mPreview);

        binding.frameLayout.setOnTouchListener((v, event) -> {
            // Dokunmanın odaklama yapmasını istiyoruz
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus(null);
            return true;
        });
    }

    public static Camera getCameraInstance()
    {
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e)
        {
        }
        return c;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null)
        {
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(this, mCamera);
            binding.frameLayout.addView(mPreview);
        }
    }
}