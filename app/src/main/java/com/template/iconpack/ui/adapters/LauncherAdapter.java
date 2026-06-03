package com.template.iconpack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;

import java.util.Arrays;
import java.util.List;

public class LauncherAdapter extends RecyclerView.Adapter<LauncherAdapter.ViewHolder> {

    public interface OnLauncherClickListener {
        void onLauncherClick(String launcherName);
    }

    private final List<String> launchers;
    private final OnLauncherClickListener listener;

    public LauncherAdapter(OnLauncherClickListener listener) {
        this.listener = listener;
        this.launchers = Arrays.asList(
                "Nova Launcher",
                "Lawnchair",
                "Microsoft Launcher",
                "Smart Launcher",
                "Niagara Launcher",
                "Action Launcher",
                "Hyperion Launcher",
                "Poco Launcher"
        );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_launcher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = launchers.get(position);
        holder.name.setText(name);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLauncherClick(name);
        });
    }

    @Override
    public int getItemCount() {
        return launchers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.launcher_name);
        }
    }
}
