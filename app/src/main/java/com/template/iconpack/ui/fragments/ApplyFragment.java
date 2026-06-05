package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.ui.adapters.LauncherAdapter;

public class ApplyFragment extends Fragment {

    private RecyclerView launcherList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply, container, false);
        launcherList = view.findViewById(R.id.launcher_list);
        launcherList.setLayoutManager(new LinearLayoutManager(getContext()));

        LauncherAdapter adapter = new LauncherAdapter(launcherName -> {
            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "请在 " + launcherName + " 设置中应用此图标包",
                        Toast.LENGTH_LONG).show();
            }
        });

        launcherList.setAdapter(adapter);
        
        TextView tv = view.findViewById(R.id.page_title);
        if (tv != null) tv.setText(getString(R.string.apply_title));
return view;
    }
}
