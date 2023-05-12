package com.serkantken.ametist.utilities;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener
{
    private final GestureDetector gestureDetector;
    private View view;
    private Boolean isSwiping = false;
    private float dX, startX;

    public OnSwipeTouchListener(Context context, View view) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        this.view = view;
    }

    public void onSwipeLeft() {
        float translationX = view.getTranslationX();
        float swipeDistance = view.getWidth() * 0.2f; // Görüntü genişliğinin %20'si kadar kaydır

        // Görüntüyü sola kaydır
        if (translationX < swipeDistance) {
            view.setTranslationX(translationX + swipeDistance);
        }
    }

    public void onSwipeRight() {
        float translationX = view.getTranslationX();
        float swipeDistance = view.getWidth() * 0.2f; // Görüntü genişliğinin %20'si kadar kaydır

        // Görüntüyü sağa kaydır
        if (translationX > -swipeDistance) {
            view.setTranslationX(translationX - swipeDistance);
        }
    }

    public void onClick() {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                startX = event.getRawX();
                isSwiping = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                view.animate()
                        .x(event.getRawX() + dX)
                        .setDuration(0)
                        .start();
                isSwiping = true;
                return true;

            case MotionEvent.ACTION_UP:
                float newX = event.getRawX() + dX;
                if (newX > view.getWidth() / 2)
                {
                    onSwipeRight();
                }
                else if (newX < -view.getWidth() / 2)
                {
                    onSwipeLeft();
                }
                else
                {
                    if (!isSwiping)
                    {
                        onClick();
                    }
                    isSwiping = false;
                }
                view.animate()
                        .x(0)
                        .setDuration(500)
                        .start();

                return true;

            default:
                return false;
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}
