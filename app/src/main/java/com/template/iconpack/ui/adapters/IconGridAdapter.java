package com.template.iconpack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.DrawableInfo;

import java.util.ArrayList;
import java.util.List;

public class IconGridAdapter extends RecyclerView.Adapter<IconGridAdapter.ViewHolder> {

    private final List<DrawableInfo> icons;
    private final List<DrawableInfo> filteredIcons;
    private final boolean showName;
    private String filterText = "";

    public IconGridAdapter(List<DrawableInfo> icons, boolean showName) {
        this.icons = icons;
        this.filteredIcons = new ArrayList<>(icons);
        this.showName = showName;
    }

    public void filter(String text) {
        filterText = text.toLowerCase();
        filteredIcons.clear();
        if (filterText.isEmpty()) {
            filteredIcons.addAll(icons);
        } else {
            for (DrawableInfo d : icons) {
                if (d.name.toLowerCase().contains(filterText)) {
                    filteredIcons.add(d);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icon_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DrawableInfo icon = filteredIcons.get(position);
        try {
            holder.image.setImageResource(icon.resId);
        } catch (Exception e) {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        holder.name.setText(icon.name);
        holder.name.setVisibility(showName ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return filteredIcons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.icon_image);
            name = itemView.findViewById(R.id.icon_name);
        }
    }
}
