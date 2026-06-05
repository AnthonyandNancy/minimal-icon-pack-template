package com.template.iconpack.ui.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Reusable animation helpers.
 */
public final class GlassAnimations {

    private static final float ANIM_PRESS_SCALE    = 0.97f;
    private static final long  ANIM_CARD_CLICK     = 120L;
    private static final long  ANIM_PAGE_TRANSITION = 220L;
    private static final long  ANIM_CARD_STAGGER   = 60L;
    private static final long  ANIM_DRAWER_OPEN    = 260L;

    private GlassAnimations() {}

    /**
     * Scale-down on press, scale-back on release.
     */
    public static void applyPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(ANIM_PRESS_SCALE)
                            .scaleY(ANIM_PRESS_SCALE)
                            .setDuration(ANIM_CARD_CLICK)
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(ANIM_CARD_CLICK)
                            .start();
                    break;
            }
            return false;
        });
    }

    /**
     * Page entrance animation: alpha 0→1 + slight slide up.
     */
    public static void animatePageEntrance(View view) {
        view.setAlpha(0f);
        view.setTranslationY(12f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIM_PAGE_TRANSITION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Staggered card entrance — call for each card with increasing delay.
     */
    public static void animateCardEntrance(View card, int index) {
        card.setAlpha(0f);
        card.setTranslationY(20f);
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220)
                .setStartDelay(index * ANIM_CARD_STAGGER)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Overshoot pop-in (for dialogs, bottom sheets).
     */
    public static void animatePopIn(View view) {
        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(280)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
    }

    /**
     * Drawer slide-in (alpha fade on scrim).
     */
    public static void animateDrawerScrim(View scrim, boolean show) {
        scrim.animate()
                .alpha(show ? 1f : 0f)
                .setDuration(ANIM_DRAWER_OPEN)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Fade-in helper.
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(duration).start();
    }

    /**
     * Fade-out helper.
     */
    public static void fadeOut(View view, long duration) {
        view.animate().alpha(0f).setDuration(duration).start();
    }
}
