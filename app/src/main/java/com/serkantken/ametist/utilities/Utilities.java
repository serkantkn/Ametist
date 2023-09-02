package com.serkantken.ametist.utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class Utilities
{
    Context context;
    Activity activity;

    public Utilities(Context context, Activity activity)
    {
        this.context = context;
        this.activity = activity;
    }

    public Utilities(Context context)
    {
        this.context = context;
    }

    public int getStatusBarHeight()
    {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public int getNavigationBarHeight(int orientation)
    {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
        if (resourceId > 0)
        {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int convertDpToPixel(int dp)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(dp * density);
    }

    public void setMargins(View view, int start, int top, int end, int bottom)
    {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(start, top, end, bottom);
        view.setLayoutParams(layoutParams);
    }

    public void blur(BlurView view, float radius, boolean isRounded)
    {
        View decorView = activity.getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();

        view.setupWith(rootView, new RenderScriptBlur(activity))
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
        if (isRounded)
        {
            view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            view.setClipToOutline(true);
        }
    }

    public boolean isMIUI()
    {
        String miui = "";
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equalsIgnoreCase("Xiaomi"))
        {
            try {
                @SuppressLint("PrivateApi") Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
                Method getMethod = systemPropertiesClass.getMethod("get", String.class);
                miui = (String) getMethod.invoke(null, "ro.miui.ui.version.name");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return !Objects.equals(miui, "");
    }
}
