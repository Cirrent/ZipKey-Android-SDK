package com.sampleapp.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;


public class ImageViewHeightCustomRatio extends android.support.v7.widget.AppCompatImageView {

    private final static double ASPECT_RATIO = 1.79;


    public ImageViewHeightCustomRatio(Context context) {
        super(context);
    }

    public ImageViewHeightCustomRatio(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewHeightCustomRatio(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);

        if (width == 0) {
            String msg = "Both width and height should not be zero - watch out for scrollable containers";
            Log.e(this.getClass().getSimpleName(), msg);
        }

        int height = (int) (width / ASPECT_RATIO);

        // Ask children to follow the new preview dimension.
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

}
