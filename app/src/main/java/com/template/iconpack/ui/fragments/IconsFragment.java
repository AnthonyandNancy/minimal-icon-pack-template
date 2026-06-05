package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Dialog;
import android.graphics.Color;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.ui.adapters.IconGridAdapter;
import com.template.iconpack.utils.IconPackLoader;
import com.template.iconpack.utils.PreferencesHelper;

import java.util.List;

public class IconsFragment extends Fragment {

    private RecyclerView iconsGrid;
    private TextView emptyView;
    private TextView noResultsView;
    private EditText searchInput;
    private IconGridAdapter adapter;
    private PreferencesHelper prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons, container, false);
        if (getContext() == null) return view;
        TextView tv = view.findViewById(R.id.page_title);
        if (tv != null) tv.setText(getString(R.string.icons_title));

        prefs = new PreferencesHelper(getContext());

        iconsGrid = view.findViewById(R.id.icons_grid);
        emptyView = view.findViewById(R.id.icons_empty);
        noResultsView = view.findViewById(R.id.icons_no_results);
        searchInput = view.findViewById(R.id.icons_search);

        List<DrawableInfo> icons = IconPackLoader.loadDrawables(getContext());

        if (icons.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            iconsGrid.setVisibility(View.GONE);
            searchInput.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            iconsGrid.setVisibility(View.VISIBLE);
            searchInput.setVisibility(View.VISIBLE);

            int columns = prefs.getIconColumns();
            iconsGrid.setLayoutManager(new GridLayoutManager(getContext(), columns));

            boolean showName = prefs.isShowIconName();
            adapter = new IconGridAdapter(icons, showName);
            adapter.setOnIconClickListener(icon -> showIconZoom(icon));
            iconsGrid.setAdapter(adapter);

            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        int count2 = adapter.filter(s.toString());
                        if (noResultsView != null)
                            noResultsView.setVisibility(count2 == 0 ? View.VISIBLE : View.GONE);
                        if (iconsGrid != null)
                            iconsGrid.setVisibility(count2 == 0 ? View.GONE : View.VISIBLE);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }

    public void refresh() {
        if (getContext() == null) return;
        List<DrawableInfo> icons = IconPackLoader.loadDrawables(getContext());
        if (icons.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (iconsGrid != null) iconsGrid.setVisibility(View.GONE);
            if (searchInput != null) searchInput.setVisibility(View.GONE);
            if (noResultsView != null) noResultsView.setVisibility(View.GONE);
        } else {
            if (emptyView != null) emptyView.setVisibility(View.GONE);
            if (searchInput != null) searchInput.setVisibility(View.VISIBLE);
            if (iconsGrid != null) {
                iconsGrid.setVisibility(View.VISIBLE);
                int columns = prefs.getIconColumns();
                iconsGrid.setLayoutManager(new GridLayoutManager(getContext(), columns));
                adapter = new IconGridAdapter(icons, prefs.isShowIconName());
                adapter.setOnIconClickListener(icon -> showIconZoom(icon));
                iconsGrid.setAdapter(adapter);
                if (searchInput != null) {
                    String q = searchInput.getText() != null ? searchInput.getText().toString() : "";
                    if (!q.isEmpty()) {
                        int count2 = adapter.filter(q);
                        if (noResultsView != null)
                            noResultsView.setVisibility(count2 == 0 ? View.VISIBLE : View.GONE);
                    }
                }
            }
        }
    }

    private void showIconZoom(DrawableInfo icon) {
        Dialog d = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(getContext());
        iv.setImageResource(icon.resId);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackgroundColor(0x80000000);
        iv.setPadding(32,32,32,32);
        iv.setOnClickListener(v -> d.dismiss());

        ScaleAnimation anim = new ScaleAnimation(
                0.7f, 1.0f, 0.7f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(250);
        anim.setFillAfter(true);
        iv.startAnimation(anim);

        d.setContentView(iv);
        d.show();
    }
}
