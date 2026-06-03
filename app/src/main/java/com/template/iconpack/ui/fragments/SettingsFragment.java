package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.template.iconpack.R;
import com.template.iconpack.utils.PreferencesHelper;

public class SettingsFragment extends Fragment {

    private PreferencesHelper prefs;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchShowIconName;
    private TextView columnsValue;
    private int currentColumns = 4;

    public interface SettingsCallback {
        void onSettingChanged();
    }

    private SettingsCallback callback;

    public void setCallback(SettingsCallback callback) {
        this.callback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        if (getContext() == null) return view;

        prefs = new PreferencesHelper(getContext());

        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchShowIconName = view.findViewById(R.id.switch_show_icon_name);
        columnsValue = view.findViewById(R.id.columns_value);

        // Load current settings
        switchDarkMode.setChecked(prefs.isDarkMode());
        switchShowIconName.setChecked(prefs.isShowIconName());
        currentColumns = prefs.getIconColumns();
        columnsValue.setText(String.valueOf(currentColumns));

        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Show icon name toggle
        switchShowIconName.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setShowIconName(isChecked);
            if (callback != null) callback.onSettingChanged();
        });

        // Columns setting (click to cycle: 3 -> 4 -> 5 -> 6 -> 3)
        columnsValue.setOnClickListener(v -> {
            currentColumns = currentColumns >= 6 ? 3 : currentColumns + 1;
            prefs.setIconColumns(currentColumns);
            columnsValue.setText(String.valueOf(currentColumns));
            if (callback != null) callback.onSettingChanged();
        });

        // Clear cache
        view.findViewById(R.id.btn_clear_cache).setOnClickListener(v -> {
            prefs.clearCache();
            Toast.makeText(getContext(), R.string.settings_cache_cleared, Toast.LENGTH_SHORT).show();
        });

        // Reload
        view.findViewById(R.id.btn_reload).setOnClickListener(v -> {
            if (callback != null) callback.onSettingChanged();
            Toast.makeText(getContext(), "图标数据已重新加载", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
