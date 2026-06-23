package com.template.iconpack.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.ChangelogEntry;
import com.template.iconpack.ui.adapters.ChangelogAdapter;
import com.template.iconpack.utils.IconPackLoader;

import java.util.List;

public class ChangelogFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_changelog, container, false);

        TextView title = view.findViewById(R.id.page_title);
        if (title != null) title.setText(getString(R.string.menu_changelog));

        RecyclerView listView = view.findViewById(R.id.changelog_list);
        TextView emptyView = view.findViewById(R.id.changelog_empty);

        ChangelogAdapter adapter = new ChangelogAdapter();
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(adapter);

        List<ChangelogEntry> items = IconPackLoader.loadChangelog(requireContext());
        adapter.setItems(items);

        boolean empty = items == null || items.isEmpty();
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);

        return view;
    }
}
