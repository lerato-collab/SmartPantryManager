package com.example.smartpantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Custom adapter that binds pantry rows from the database to the RecyclerView. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items;
    private final Listener listener;
    private final boolean alertsEnabled;

    public PantryAdapter(List<PantryItem> items, boolean alertsEnabled, Listener listener) {
        this.items = items;
        this.alertsEnabled = alertsEnabled;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PantryItem item = items.get(position);
        h.name.setText(item.name);
        h.qty.setText(UnitUtil.formatQty(item.quantity) + " " + item.unit);

        if (item.expiry == null || item.expiry.isEmpty()) {
            h.expiry.setVisibility(View.GONE);
        } else {
            h.expiry.setVisibility(View.VISIBLE);
            boolean soon = alertsEnabled && expiresSoon(item.expiry);
            h.expiry.setText("Expires: " + item.expiry + (soon ? "  (expiring soon!)" : ""));
            h.expiry.setTextColor(soon ? Color.RED : Color.GRAY);
        }
        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** True when the expiry date is within the next 3 days (or already passed). */
    private boolean expiresSoon(String expiry) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expiry);
            long days = TimeUnit.MILLISECONDS.toDays(d.getTime() - System.currentTimeMillis());
            return days <= 3;
        } catch (Exception e) {
            return false;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, qty, expiry;
        ImageButton delete;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_item_name);
            qty = v.findViewById(R.id.tv_item_qty);
            expiry = v.findViewById(R.id.tv_item_expiry);
            delete = v.findViewById(R.id.btn_delete);
        }
    }
}
