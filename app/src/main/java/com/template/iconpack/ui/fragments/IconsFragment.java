package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.ui.BackBarHelper;
import com.template.iconpack.models.DrawableInfo;
import com.template.iconpack.ui.adapters.IconGridAdapter;
import com.template.iconpack.utils.IconPackLoader;
import com.template.iconpack.utils.PreferencesHelper;

import java.util.List;

public class IconsFragment extends Fragment {

    private RecyclerView iconsGrid;
    private TextView emptyView;
    private EditText searchInput;
    private IconGridAdapter adapter;
    private PreferencesHelper prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons, container, false);
        if (getContext() == null) return view;

        prefs = new PreferencesHelper(getContext());

        iconsGrid = view.findViewById(R.id.icons_grid);
        emptyView = view.findViewById(R.id.icons_empty);
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
            iconsGrid.setAdapter(adapter);

            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) adapter.filter(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        BackBarHelper.setup(view, getActivity(), getString(R.string.icons_title));
        return view;
    }

    public void refresh() {
        if (getContext() == null) return;
        List<DrawableInfo> icons = IconPackLoader.loadDrawables(getContext());
        if (icons.isEmpty()) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (iconsGrid != null) iconsGrid.setVisibility(View.GONE);
        } else {
            if (emptyView != null) emptyView.setVisibility(View.GONE);
            if (iconsGrid != null) {
                iconsGrid.setVisibility(View.VISIBLE);
                int columns = prefs.getIconColumns();
                iconsGrid.setLayoutManager(new GridLayoutManager(getContext(), columns));
                adapter = new IconGridAdapter(icons, prefs.isShowIconName());
                iconsGrid.setAdapter(adapter);
            }
        }
    }
}
