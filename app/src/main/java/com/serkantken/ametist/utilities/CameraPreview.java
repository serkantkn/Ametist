package com.serkantken.ametist.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // SurfaceHolder'ı alın ve geri aramaları ayarlayın
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Surface hazır olduğunda, kameranın önizlemesini başlatın
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("CameraPreview", "Hata: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surface değiştiğinde, önizlemeyi güncelleyin
        if (mHolder.getSurface() == null) {
            // Surface yoksa, geri dön
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // preview durdurulamazsa devam et
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("CameraPreview", "Hata: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface yok edildiğinde, önizlemeyi durdurun
        mCamera.stopPreview();
    }
}
