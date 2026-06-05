package com.template.iconpack.ui.glass;

import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Spring-based elastic press animation.
 * Press: scale 0.97. Release: spring back with overshoot.
 */
public final class GlassMotion {

    private GlassMotion() {}

    public static void attach(View view, float elasticity) {
        if (view == null) return;
        final float tension = 0.5f + elasticity * 2.0f; // 0.5~1.2

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animateSpring(v, tension);
                    break;
            }
            return false;
        });
    }

    private static void animateSpring(View v, float tension) {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(220);
        anim.setInterpolator(new DecelerateInterpolator(tension));
        anim.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            // Spring overshoot: goes past 1.0 for bouncy feel
            float spring = 1f + (float)(Math.sin(t * Math.PI * 1.5) * Math.exp(-t * 3.0) * 0.025);
            v.setScaleX(spring);
            v.setScaleY(spring);
        });
        anim.start();
    }
}
