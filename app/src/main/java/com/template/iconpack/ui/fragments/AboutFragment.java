package com.template.iconpack.ui.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.template.iconpack.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView tv = view.findViewById(R.id.page_title);
        if (tv != null) tv.setText(getString(R.string.about_title));

        // Show actual version
        try {
            PackageInfo pi = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            ((TextView) view.findViewById(R.id.about_version)).setText("v" + pi.versionName);
        } catch (Exception ignored) {}

        view.findViewById(R.id.btn_about_share).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message));
            startActivity(Intent.createChooser(intent, getString(R.string.about_share)));
        });

        return view;
    }
}
