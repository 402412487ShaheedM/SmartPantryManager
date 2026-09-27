package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemClickListener {

    private RecyclerView recyclerViewPantry;
    private PantryAdapter pantryAdapter;
    private DatabaseHelper databaseHelper;

    private ArrayList<PantryItem> pantryItems = new ArrayList<>();

    private Button buttonAdd, buttonAll, buttonLow, buttonExpiring, buttonExpired;
    private EditText editSearch;

    private TextView textTotalItems, textLowStockCount, textExpiringCount, textExpiredCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);

        buttonAdd = findViewById(R.id.buttonAdd);
        buttonAll = findViewById(R.id.buttonAll);
        buttonLow = findViewById(R.id.buttonLow);
        buttonExpiring = findViewById(R.id.buttonExpiring);
        buttonExpired = findViewById(R.id.buttonExpired);

        editSearch = findViewById(R.id.editSearch);

        textTotalItems = findViewById(R.id.textTotalItems);
        textLowStockCount = findViewById(R.id.textLowStockCount);
        textExpiringCount = findViewById(R.id.textExpiringCount);
        textExpiredCount = findViewById(R.id.textExpiredCount);

        buttonAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddIngredientActivity.class)));

        buttonAll.setOnClickListener(v -> showAllItems());

        buttonLow.setOnClickListener(v -> filterLowStock());

        buttonExpiring.setOnClickListener(v -> filterExpiring());

        buttonExpired.setOnClickListener(v -> filterExpired());

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        pantryItems = databaseHelper.getAllPantryItems();
        pantryAdapter = new PantryAdapter(pantryItems, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
        updateStatistics();
    }

    private void showAllItems() {
        pantryAdapter = new PantryAdapter(pantryItems, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    // COMMIT 7: Search by name and category
    private void filterSearch(String text) {

        ArrayList<PantryItem> filtered = new ArrayList<>();

        String search = text.toLowerCase().trim();

        for (PantryItem item : pantryItems) {

            if (item.getName().toLowerCase().contains(search) ||
                    item.getCategory().toLowerCase().contains(search)) {

                filtered.add(item);
            }
        }

        pantryAdapter = new PantryAdapter(filtered, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void filterLowStock() {

        ArrayList<PantryItem> filtered = new ArrayList<>();

        for (PantryItem item : pantryItems) {

            int qty = extractQuantity(item.getQuantity());

            if (qty <= 5) {
                filtered.add(item);
            }
        }

        pantryAdapter = new PantryAdapter(filtered, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void filterExpiring() {

        ArrayList<PantryItem> filtered = new ArrayList<>();

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date();

            for (PantryItem item : pantryItems) {

                Date expiry = sdf.parse(item.getExpiry());

                long days = TimeUnit.MILLISECONDS.toDays(expiry.getTime() - today.getTime());

                if (days >= 0 && days <= 7) {
                    filtered.add(item);
                }
            }

        } catch (Exception ignored) { }

        pantryAdapter = new PantryAdapter(filtered, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void filterExpired() {

        ArrayList<PantryItem> filtered = new ArrayList<>();

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date();

            for (PantryItem item : pantryItems) {

                Date expiry = sdf.parse(item.getExpiry());

                if (expiry.before(today)) {
                    filtered.add(item);
                }
            }

        } catch (Exception ignored) { }

        pantryAdapter = new PantryAdapter(filtered, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void updateStatistics() {

        int total = pantryItems.size();
        int low = 0;
        int expiring = 0;
        int expired = 0;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date();

            for (PantryItem item : pantryItems) {

                int qty = extractQuantity(item.getQuantity());

                if (qty <= 5) {
                    low++;
                }

                Date expiry = sdf.parse(item.getExpiry());

                long days = TimeUnit.MILLISECONDS.toDays(expiry.getTime() - today.getTime());

                if (days < 0) {
                    expired++;
                } else if (days <= 7) {
                    expiring++;
                }
            }

        } catch (Exception ignored) { }

        textTotalItems.setText(String.valueOf(total));
        textLowStockCount.setText(String.valueOf(low));
        textExpiringCount.setText(String.valueOf(expiring));
        textExpiredCount.setText(String.valueOf(expired));
    }

    private int extractQuantity(String quantity) {

        String number = quantity.replaceAll("[^0-9]", "");

        if (number.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(number);
    }

    @Override
    public void onEditClick(PantryItem item) {

        Intent intent = new Intent(this, AddIngredientActivity.class);

        intent.putExtra("id", item.getId());
        intent.putExtra("name", item.getName());
        intent.putExtra("quantity", item.getQuantity());
        intent.putExtra("expiry", item.getExpiry());
        intent.putExtra("category", item.getCategory());

        startActivity(intent);
    }

    @Override
    public void onDeleteClick(PantryItem item) {

        databaseHelper.deletePantryItem(item.getId());
        loadPantryItems();
    }
}