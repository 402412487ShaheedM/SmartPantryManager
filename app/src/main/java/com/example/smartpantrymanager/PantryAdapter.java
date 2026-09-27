
package com.example.smartpantrymanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private final ArrayList<PantryItem> pantryList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(PantryItem item);
        void onDeleteClick(PantryItem item);
    }

    public PantryAdapter(ArrayList<PantryItem> pantryList, OnItemClickListener listener) {
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
        holder.textCategory.setText(item.getCategory());
        holder.textQuantity.setText("Quantity: " + item.getQuantity());
        holder.textExpiry.setText("Expiry: " + item.getExpiry());

        int qty = extractQuantity(item.getQuantity());

        if (qty <= 5) {
            holder.textLowStock.setVisibility(View.VISIBLE);
        } else {
            holder.textLowStock.setVisibility(View.GONE);
        }

        setStatus(holder, item.getExpiry());

        holder.buttonEdit.setOnClickListener(v -> listener.onEditClick(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return pantryList.size();
    }

    private int extractQuantity(String quantity) {

        String number = quantity.replaceAll("[^0-9]", "");

        if (number.isEmpty()) return 0;

        return Integer.parseInt(number);
    }

    private void setStatus(PantryViewHolder holder, String expiryDate) {

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Date today = new Date();
            Date expiry = sdf.parse(expiryDate);

            long days = TimeUnit.MILLISECONDS.toDays(expiry.getTime() - today.getTime());

            if (days < 0) {

                holder.textStatus.setText("Expired");
                holder.textStatus.setBackgroundColor(Color.parseColor("#FDECEC"));
                holder.textStatus.setTextColor(Color.parseColor("#C62828"));

            } else if (days <= 7) {

                holder.textStatus.setText("Expiring");
                holder.textStatus.setBackgroundColor(Color.parseColor("#FFF4E5"));
                holder.textStatus.setTextColor(Color.parseColor("#EF6C00"));

            } else {

                holder.textStatus.setText("Fresh");
                holder.textStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.textStatus.setTextColor(Color.parseColor("#2E7D32"));
            }

        } catch (Exception e) {

            holder.textStatus.setText("Unknown");
            holder.textStatus.setBackgroundColor(Color.LTGRAY);
            holder.textStatus.setTextColor(Color.DKGRAY);
        }
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {

        TextView textIngredientName;
        TextView textCategory;
        TextView textQuantity;
        TextView textExpiry;
        TextView textStatus;
        TextView textLowStock;

        Button buttonEdit;
        Button buttonDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            textIngredientName = itemView.findViewById(R.id.textIngredientName);
            textCategory = itemView.findViewById(R.id.textCategory);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            textStatus = itemView.findViewById(R.id.textStatus);
            textLowStock = itemView.findViewById(R.id.textLowStock);

            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}