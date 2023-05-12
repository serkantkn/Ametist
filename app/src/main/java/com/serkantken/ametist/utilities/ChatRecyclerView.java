package com.serkantken.ametist.utilities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ChatRecyclerView extends RecyclerView implements SwipeListener
{
    boolean isSwiping;

    public ChatRecyclerView(@NonNull Context context)
    {
        super(context);
    }

    public ChatRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ChatRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e)
    {
        if (isSwiping)
            return false;
        else
            return super.onInterceptTouchEvent(e);
    }

    @Override
    public void onSwipeHorizontal(boolean isSwiping) {
        this.isSwiping = isSwiping;
    }
}
