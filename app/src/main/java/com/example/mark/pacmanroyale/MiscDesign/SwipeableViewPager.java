package com.example.mark.pacmanroyale.MiscDesign;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Mark on 16/03/2018.
 */

public class SwipeableViewPager extends ViewPager {

    public boolean mIsSwipingEnabled = true;

    public SwipeableViewPager(@NonNull Context context) {
        super(context);
    }

    public SwipeableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mIsSwipingEnabled) {
            return super.onTouchEvent(ev);
        }
        return false;
    }

}
