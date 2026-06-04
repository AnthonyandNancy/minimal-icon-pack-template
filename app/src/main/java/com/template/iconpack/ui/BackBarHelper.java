package com.template.iconpack.ui;

import android.app.Activity;
import android.view.View;

import com.template.iconpack.R;
import com.template.iconpack.MainActivity;

/**
 * One-line back-bar wiring for all sub-page fragments.
 * Call in fragment onCreateView: BackBarHelper.setup(view, getActivity());
 */
public final class BackBarHelper {
    private BackBarHelper() {}

    public static void setup(View root, Activity activity) {
        if (root == null || activity == null) return;
        View btn = root.findViewById(R.id.btn_back);
        if (btn != null) btn.setOnClickListener(v -> activity.onBackPressed());

        View spacer = root.findViewById(R.id.status_spacer);
        if (spacer != null && activity instanceof MainActivity) {
            MainActivity ma = (MainActivity) activity;
            spacer.getLayoutParams().height = ma.getStatusBarHeight() + ma.dp(8);
            spacer.requestLayout();
        }
    }
}
