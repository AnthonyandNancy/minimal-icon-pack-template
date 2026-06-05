package com.template.iconpack.ui.glass;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Glass press/release elastic animation.
 * Press: scale 0.97. Release: overshoot 1.015 → 1.0.
 */
public final class GlassMotion {

    private GlassMotion() {}

    private static final float PRESS_SCALE  = 0.97f;
    private static final float BOUNCE_SCALE = 1.015f;
    private static final long  PRESS_DURATION  = 100L;
    private static final long  RELEASE_DURATION = 180L;

    public static void attach(View view, float elasticity) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(PRESS_SCALE).scaleY(PRESS_SCALE)
                            .setDuration(PRESS_DURATION).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    AnimatorSet set = new AnimatorSet();
                    ObjectAnimator bounce = ObjectAnimator.ofFloat(v, "scaleX",
                            1f, BOUNCE_SCALE, 1f);
                    ObjectAnimator bounceY = ObjectAnimator.ofFloat(v, "scaleY",
                            1f, BOUNCE_SCALE, 1f);
                    set.playTogether(bounce, bounceY);
                    set.setDuration(RELEASE_DURATION);
                    set.setInterpolator(new OvershootInterpolator(elasticity));
                    set.addListener(new Animator.AnimatorListener() {
                        @Override public void onAnimationEnd(Animator a) {
                            v.setScaleX(1f); v.setScaleY(1f);
                        }
                        @Override public void onAnimationStart(Animator a) {}
                        @Override public void onAnimationCancel(Animator a) {}
                        @Override public void onAnimationRepeat(Animator a) {}
                    });
                    set.start();
                    break;
            }
            return false; // allow click through
        });
    }
}
