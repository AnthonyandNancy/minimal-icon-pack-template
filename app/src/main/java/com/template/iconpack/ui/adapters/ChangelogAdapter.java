package com.template.iconpack.ui.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.template.iconpack.R;
import com.template.iconpack.models.ChangelogEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {

    private static final int COLLAPSED_CONTENT_LINES = 6;
    private static final int COLLAPSED_CONTENT_CHARS = 180;
    private static final int COLLAPSED_ICON_COUNT = 6;

    private final List<ChangelogEntry> items = new ArrayList<>();
    private final Set<Integer> expandedItems = new HashSet<>();

    public void setItems(List<ChangelogEntry> entries) {
        items.clear();
        expandedItems.clear();
        if (entries != null) items.addAll(entries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_changelog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChangelogEntry entry = items.get(position);
        boolean expanded = expandedItems.contains(entry.versionCode);

        String title = entry.title;
        if (TextUtils.isEmpty(title)) {
            title = TextUtils.isEmpty(entry.versionName) ? "本次更新" : "v" + entry.versionName + " 更新";
        }
        holder.title.setText(title);
        holder.meta.setText(buildMeta(entry));

        bindOptionalText(holder.content, buildContentText(entry.content, expanded));
        bindOptionalText(holder.icons, buildIconsText(entry.icons, expanded));

        boolean expandable = hasMoreContent(entry.content) || hasMoreIcons(entry.icons);
        holder.toggle.setVisibility(expandable ? View.VISIBLE : View.GONE);
        holder.toggle.setText(expanded ? "收起" : "展开全部");
        View.OnClickListener toggleListener = v -> {
            if (expandedItems.contains(entry.versionCode)) {
                expandedItems.remove(entry.versionCode);
            } else {
                expandedItems.add(entry.versionCode);
            }
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(adapterPosition);
            }
        };
        holder.toggle.setOnClickListener(expandable ? toggleListener : null);
        holder.itemView.setOnClickListener(expandable ? toggleListener : null);
        holder.itemView.setClickable(expandable);

        holder.topLine.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.bottomLine.setVisibility(position == items.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String buildMeta(ChangelogEntry entry) {
        List<String> parts = new ArrayList<>();
        if (!TextUtils.isEmpty(entry.versionName)) parts.add("v" + entry.versionName);
        if (!TextUtils.isEmpty(entry.date)) parts.add(entry.date);
        return TextUtils.join(" · ", parts);
    }

    private String buildContentText(String content, boolean expanded) {
        if (TextUtils.isEmpty(content)) return "";
        if (expanded || !hasMoreContent(content)) return content;

        String[] lines = content.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        StringBuilder sb = new StringBuilder();
        int lineCount = Math.min(COLLAPSED_CONTENT_LINES, lines.length);
        for (int i = 0; i < lineCount; i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines[i]);
        }
        if (lines.length > COLLAPSED_CONTENT_LINES) {
            sb.append('\n').append("... 还有 ")
                    .append(lines.length - COLLAPSED_CONTENT_LINES)
                    .append(" 行");
            return sb.toString();
        }
        if (sb.length() > COLLAPSED_CONTENT_CHARS) {
            return sb.substring(0, COLLAPSED_CONTENT_CHARS).trim() + "\n... 内容较多";
        }
        return sb.toString();
    }

    private boolean hasMoreContent(String content) {
        if (TextUtils.isEmpty(content)) return false;
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        return normalized.split("\n", -1).length > COLLAPSED_CONTENT_LINES
                || normalized.length() > COLLAPSED_CONTENT_CHARS;
    }

    private String buildIconsText(List<String> icons, boolean expanded) {
        if (icons == null || icons.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("更新图标：");
        int totalIcons = countIcons(icons);
        int limit = expanded ? totalIcons : Math.min(COLLAPSED_ICON_COUNT, totalIcons);
        int shown = 0;
        for (String icon : icons) {
            if (TextUtils.isEmpty(icon)) continue;
            if (shown >= limit) break;
            sb.append('\n').append("• ").append(icon);
            shown++;
        }
        if (!expanded && totalIcons > COLLAPSED_ICON_COUNT) {
            sb.append('\n').append("... 还有 ")
                    .append(totalIcons - COLLAPSED_ICON_COUNT)
                    .append(" 个图标");
        }
        return sb.toString();
    }

    private boolean hasMoreIcons(List<String> icons) {
        return countIcons(icons) > COLLAPSED_ICON_COUNT;
    }

    private int countIcons(List<String> icons) {
        if (icons == null) return 0;
        int count = 0;
        for (String icon : icons) {
            if (!TextUtils.isEmpty(icon)) count++;
        }
        return count;
    }

    private void bindOptionalText(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
            view.setText("");
        } else {
            view.setVisibility(View.VISIBLE);
            view.setText(text);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View topLine;
        View bottomLine;
        TextView title;
        TextView meta;
        TextView content;
        TextView icons;
        TextView toggle;

        ViewHolder(View itemView) {
            super(itemView);
            topLine = itemView.findViewById(R.id.timeline_line_top);
            bottomLine = itemView.findViewById(R.id.timeline_line_bottom);
            title = itemView.findViewById(R.id.changelog_item_title);
            meta = itemView.findViewById(R.id.changelog_item_meta);
            content = itemView.findViewById(R.id.changelog_item_content);
            icons = itemView.findViewById(R.id.changelog_item_icons);
            toggle = itemView.findViewById(R.id.changelog_item_toggle);
        }
    }
}
