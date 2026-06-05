package com.template.iconpack.ui;

import android.app.Activity;
import android.view.View;

import com.template.iconpack.R;
import com.template.iconpack.MainActivity;
import com.template.iconpack.ui.glass.GlassMaterialFactory;
import com.template.iconpack.ui.glass.LiquidGlassDrawable;

/**
 * One-line back-bar wiring for all sub-page fragments.
 * Applies LiquidGlassDrawable to the back bar pill + click listener + status spacer.
 */
public final class BackBarHelper {
    private BackBarHelper() {}

    public static void setup(View root, Activity activity) {
        if (root == null || activity == null) return;

        View btn = root.findViewById(R.id.btn_back);
        if (btn != null) btn.setOnClickListener(v -> activity.onBackPressed());

        // Apply new LiquidGlassDrawable to the glass pill bar
        View pill = root.findViewById(R.id.backbar_pill);
        if (pill != null) {
            float density = activity.getResources().getDisplayMetrics().density;
            pill.setBackground(new LiquidGlassDrawable(GlassMaterialFactory.regular(28f), density));
        }

        View spacer = root.findViewById(R.id.status_spacer);
        if (spacer != null && activity instanceof MainActivity) {
            MainActivity ma = (MainActivity) activity;
            spacer.getLayoutParams().height = ma.getStatusBarHeight() + ma.dp(8);
            spacer.requestLayout();
        }
    }
}
