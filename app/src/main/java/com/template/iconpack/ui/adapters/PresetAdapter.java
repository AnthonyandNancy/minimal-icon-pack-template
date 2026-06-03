package com.template.iconpack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.PresetInfo;

import java.util.List;

public class PresetAdapter extends RecyclerView.Adapter<PresetAdapter.ViewHolder> {

    private final List<PresetInfo> presets;

    public PresetAdapter(List<PresetInfo> presets) {
        this.presets = presets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_preset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PresetInfo preset = presets.get(position);
        holder.name.setText(preset.name);
        holder.shape.setText(preset.iconShape);
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView shape;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.preset_name);
            shape = itemView.findViewById(R.id.preset_shape);
        }
    }
}
