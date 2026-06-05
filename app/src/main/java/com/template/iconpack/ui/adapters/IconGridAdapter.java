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
import com.template.iconpack.ui.anim.GlassAnimations;

import java.util.ArrayList;
import java.util.List;

public class IconGridAdapter extends RecyclerView.Adapter<IconGridAdapter.ViewHolder> {

    private List<DrawableInfo> icons;
    private List<DrawableInfo> filteredIcons;
    private boolean showName;
    private OnIconClickListener listener;

    public interface OnIconClickListener { void onIconClick(DrawableInfo icon); }

    public IconGridAdapter(List<DrawableInfo> icons, boolean showName) {
        this.icons = icons != null ? icons : new ArrayList<>();
        this.filteredIcons = new ArrayList<>(this.icons);
        this.showName = showName;
    }

    public void setOnIconClickListener(OnIconClickListener l) { this.listener = l; }

    public void updateData(List<DrawableInfo> newIcons) {
        this.icons = newIcons != null ? newIcons : new ArrayList<>();
        this.filteredIcons = new ArrayList<>(this.icons);
        notifyDataSetChanged();
    }

    public int filter(String query) {
        filteredIcons.clear();
        if (query == null || query.isEmpty()) {
            filteredIcons.addAll(icons);
        } else {
            String lowerQuery = query.toLowerCase();
            for (DrawableInfo icon : icons) {
                String searchText = (icon.label != null ? icon.label : icon.name).toLowerCase();
                if (searchText.contains(lowerQuery) || icon.name.toLowerCase().contains(lowerQuery)) {
                    filteredIcons.add(icon);
                }
            }
        }
        notifyDataSetChanged();
        return filteredIcons.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icon_grid, parent, false);
        GlassAnimations.applyPressAnimation(view);
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
        holder.name.setText(icon.label != null ? icon.label : icon.name);
        holder.name.setVisibility(showName ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onIconClick(icon);
        });
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
