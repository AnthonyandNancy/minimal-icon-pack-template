package com.template.iconpack.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.template.iconpack.R;
import com.template.iconpack.ui.BackBarHelper;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        view.findViewById(R.id.btn_about_share).setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message));
                startActivity(Intent.createChooser(intent, getString(R.string.about_share)));
            }
        });

        BackBarHelper.setup(view, getActivity());
        return view;
    }
}
