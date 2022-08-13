package com.serkantken.ametist.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class Utilities
{
    SharedPreferences preferences;
    Context context;
    Activity activity;

    public Utilities(Context context, Activity activity)
    {
        this.context = context;
        this.activity = activity;
        preferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
    }

    public Utilities(Context context)
    {
        this.context = context;
        preferences = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
    }

    public void setPreferences(String key, String value)
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getPreferences(String key)
    {
        return preferences.getString(key, null);
    }

    public void clearPreferences()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
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

    public void setMargins(View view, int start, int top, int end, int bottom)
    {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(start, top, end, bottom);
            view.requestLayout();
        }
    }

    public int convertDpToPixel(int dp)
    {
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(dp * density);
    }

    public int isEdgeToEdgeEnabled()
    {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
        if (resourceId > 0) {
            return resources.getInteger(resourceId);
        }
        return 0;
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

        view.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(activity))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);
        if (isRounded)
        {
            view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            view.setClipToOutline(true);
        }
    }
}
