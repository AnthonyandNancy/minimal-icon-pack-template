package com.template.iconpack.ui.glass;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import eightbitlab.com.blurview.BlurTarget;

/** FrameLayout that implements BlurView 3.x's BlurTarget interface. */
public class BlurTargetFrameLayout extends FrameLayout implements BlurTarget {
    public BlurTargetFrameLayout(Context c) { super(c); }
    public BlurTargetFrameLayout(Context c, AttributeSet a) { super(c, a); }
}
