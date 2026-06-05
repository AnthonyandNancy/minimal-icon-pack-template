package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.PresetInfo;
import com.template.iconpack.ui.adapters.PresetAdapter;
import com.template.iconpack.utils.IconPackLoader;

import java.util.List;

public class PresetsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_presets, container, false);

        RecyclerView presetsList = view.findViewById(R.id.presets_list);
        presetsList.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getContext() != null) {
            List<PresetInfo> presets = IconPackLoader.loadPresets(getContext());
            PresetAdapter adapter = new PresetAdapter(presets);
            presetsList.setAdapter(adapter);
        }

        return view;
    }
}
