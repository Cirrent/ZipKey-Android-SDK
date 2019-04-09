package com.sampleapp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.view.View;

public class ViewVisibilityAnimator {

    private final int animationDuration;
    private final View view;

    private boolean cyclic = false;

    public ViewVisibilityAnimator(View view, int animationDuration) {
        this.view = view;
        this.animationDuration = animationDuration;
    }

    public void startBlinking() {
        if (!cyclic) {
            cyclic = true;
            show();
        }
    }

    public void stopBlinking() {
        cyclic = false;
        hide();
    }

    private void show() {
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (cyclic) {
                                    hide();
                                }
                            }
                        }, animationDuration / 2);
                    }
                });
    }

    private void hide() {
        view.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        if (cyclic) {
                            show();
                        }
                    }
                });
    }

}
