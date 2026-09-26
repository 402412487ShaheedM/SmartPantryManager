package com.example.smartpantrymanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private final List<PantryItem> pantryList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(PantryItem item);
        void onDeleteClick(PantryItem item);
    }

    public PantryAdapter(List<PantryItem> pantryList, OnItemClickListener listener) {
        this.pantryList = pantryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {

        PantryItem item = pantryList.get(position);

        holder.textIngredientName.setText(item.getName());
        holder.textQuantity.setText("Quantity: " + item.getQuantity());
        holder.textExpiry.setText("Expiry: " + item.getExpiry());

        setExpiryStatus(holder, item.getExpiry());
        setLowStock(holder, item.getQuantity());

        holder.buttonEdit.setOnClickListener(v -> listener.onEditClick(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return pantryList.size();
    }

    private void setExpiryStatus(PantryViewHolder holder, String expiryDate) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date expiry = sdf.parse(expiryDate);
            Date today = new Date();

            long difference = expiry.getTime() - today.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(difference);

            if (days < 0) {
                holder.textStatus.setText("Expired");
                holder.textStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
                holder.textStatus.setTextColor(Color.parseColor("#C62828"));

            } else if (days <= 7) {
                holder.textStatus.setText("Expiring");
                holder.textStatus.setBackgroundColor(Color.parseColor("#FFF8E1"));
                holder.textStatus.setTextColor(Color.parseColor("#F57F17"));

            } else {
                holder.textStatus.setText("Fresh");
                holder.textStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.textStatus.setTextColor(Color.parseColor("#2E7D32"));
            }

        } catch (ParseException e) {
            holder.textStatus.setText("Unknown");
            holder.textStatus.setBackgroundColor(Color.LTGRAY);
            holder.textStatus.setTextColor(Color.DKGRAY);
        }
    }

    private void setLowStock(PantryViewHolder holder, String quantity) {

        holder.textLowStock.setVisibility(View.GONE);

        String value = quantity.toLowerCase().trim();

        try {

            if (value.contains("kg")) {
                double kg = Double.parseDouble(value.replace("kg", "").trim());
                if (kg <= 1) {
                    holder.textLowStock.setVisibility(View.VISIBLE);
                }

            } else if (value.contains("g")) {
                double grams = Double.parseDouble(value.replace("g", "").trim());
                if (grams <= 500) {
                    holder.textLowStock.setVisibility(View.VISIBLE);
                }

            } else if (value.contains("l")) {
                double litres = Double.parseDouble(value.replace("l", "").trim());
                if (litres <= 1) {
                    holder.textLowStock.setVisibility(View.VISIBLE);
                }

            } else {
                double number = Double.parseDouble(value);
                if (number <= 2) {
                    holder.textLowStock.setVisibility(View.VISIBLE);
                }
            }

        } catch (Exception ignored) {
        }
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {

        TextView textIngredientName;
        TextView textQuantity;
        TextView textExpiry;
        TextView textStatus;
        TextView textLowStock;

        Button buttonEdit;
        Button buttonDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            textIngredientName = itemView.findViewById(R.id.textIngredientName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            textStatus = itemView.findViewById(R.id.textStatus);
            textLowStock = itemView.findViewById(R.id.textLowStock);

            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}