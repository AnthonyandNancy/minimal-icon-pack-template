package com.template.iconpack.ui;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.template.iconpack.R;
import com.template.iconpack.MainActivity;
import com.template.iconpack.ui.glass.GlassMaterialFactory;
import com.template.iconpack.ui.glass.LiquidGlassDrawable;

/**
 * One-line back-bar wiring for all sub-page fragments.
 * Applies LiquidGlassDrawable to the glass pill + click listener + status spacer + title.
 */
public final class BackBarHelper {
    private BackBarHelper() {}

    public static void setup(View root, Activity activity) {
        setup(root, activity, null);
    }

    public static void setup(View root, Activity activity, String title) {
        if (root == null || activity == null) return;

        View btn = root.findViewById(R.id.btn_back);
        if (btn != null) btn.setOnClickListener(v -> activity.onBackPressed());

        // Apply glass drawable to pill bar
        View pill = root.findViewById(R.id.backbar_pill);
        if (pill != null) {
            float density = activity.getResources().getDisplayMetrics().density;
            pill.setBackground(new LiquidGlassDrawable(GlassMaterialFactory.regular(28f), density));
        }

        // Set page title
        if (title != null) {
            TextView tv = root.findViewById(R.id.page_title);
            if (tv != null) tv.setText(title);
        }

        // Status spacer
        View spacer = root.findViewById(R.id.status_spacer);
        if (spacer != null && activity instanceof MainActivity) {
            MainActivity ma = (MainActivity) activity;
            spacer.getLayoutParams().height = ma.getStatusBarHeight() + ma.dp(8);
            spacer.requestLayout();
        }
    }
}
