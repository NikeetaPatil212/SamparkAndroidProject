package com.example.androidproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.R;
import com.example.androidproject.utils.NavMenuItem;

import java.util.List;

public class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<NavMenuItem> list;

    public DrawerAdapter(List<NavMenuItem> list) {
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drawer, parent, false);
        return new DrawerVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        NavMenuItem item = list.get(position);
        DrawerVH vh = (DrawerVH) holder;

        vh.title.setText(item.title);
        vh.icon.setImageResource(item.icon);

        vh.arrow.setVisibility(item.isExpandable ? View.VISIBLE : View.GONE);
        vh.arrow.setRotation(item.isExpanded ? 180 : 0);

        vh.itemView.setOnClickListener(v -> {
            if (!item.isExpandable) return;

            if (item.isExpanded) {
                collapse(position);
            } else {
                expand(position);
            }
        });
    }

    private void expand(int pos) {
        list.get(pos).isExpanded = true;

        list.add(pos + 1, new NavMenuItem("Batch", R.drawable.baseline_add_home_work_24));
        list.add(pos + 2, new NavMenuItem("Timing", R.drawable.baseline_attach_email_24));

        notifyItemRangeInserted(pos + 1, 2);
        notifyItemChanged(pos);
    }

    private void collapse(int pos) {
        list.get(pos).isExpanded = false;

        list.remove(pos + 1);
        list.remove(pos + 1);

        notifyItemRangeRemoved(pos + 1, 2);
        notifyItemChanged(pos);
    }

    static class DrawerVH extends RecyclerView.ViewHolder {
        ImageView icon, arrow;
        TextView title;

        DrawerVH(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivIcon);
            title = itemView.findViewById(R.id.tvTitle);
            arrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}
