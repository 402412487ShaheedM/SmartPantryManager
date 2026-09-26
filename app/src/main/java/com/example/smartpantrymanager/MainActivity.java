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

public class MainActivity extends AppCompatActivity
        implements PantryAdapter.OnItemClickListener {

    private RecyclerView recyclerViewPantry;
    private Button buttonAdd;
    private EditText editSearch;

    private TextView textTotalItems;
    private TextView textLowStockCount;
    private TextView textExpiringCount;
    private TextView textExpiredCount;

    private DatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;

    private ArrayList<PantryItem> pantryItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        buttonAdd = findViewById(R.id.buttonAdd);
        editSearch = findViewById(R.id.editSearch);

        textTotalItems = findViewById(R.id.textTotalItems);
        textLowStockCount = findViewById(R.id.textLowStockCount);
        textExpiringCount = findViewById(R.id.textExpiringCount);
        textExpiredCount = findViewById(R.id.textExpiredCount);

        databaseHelper = new DatabaseHelper(this);

        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        loadPantryItems();

        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddIngredientActivity.class);
            startActivity(intent);
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
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

    private void filterItems(String text) {

        ArrayList<PantryItem> filteredList = new ArrayList<>();

        for (PantryItem item : pantryItems) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        pantryAdapter = new PantryAdapter(filteredList, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void updateStatistics() {

        int total = pantryItems.size();
        int lowStock = 0;
        int expiring = 0;
        int expired = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (PantryItem item : pantryItems) {

            String qty = item.getQuantity().toLowerCase().trim();

            try {

                if (qty.contains("kg")) {
                    double value = Double.parseDouble(qty.replace("kg", "").trim());
                    if (value <= 1) lowStock++;

                } else if (qty.contains("g")) {
                    double value = Double.parseDouble(qty.replace("g", "").trim());
                    if (value <= 500) lowStock++;

                } else if (qty.contains("l")) {
                    double value = Double.parseDouble(qty.replace("l", "").trim());
                    if (value <= 1) lowStock++;

                } else {
                    double value = Double.parseDouble(qty);
                    if (value <= 2) lowStock++;
                }

            } catch (Exception ignored) { }

            try {

                Date expiry = sdf.parse(item.getExpiry());
                long days = TimeUnit.MILLISECONDS.toDays(expiry.getTime() - new Date().getTime());

                if (days < 0) {
                    expired++;
                } else if (days <= 7) {
                    expiring++;
                }

            } catch (Exception ignored) { }
        }

        textTotalItems.setText(String.valueOf(total));
        textLowStockCount.setText(String.valueOf(lowStock));
        textExpiringCount.setText(String.valueOf(expiring));
        textExpiredCount.setText(String.valueOf(expired));
    }

    @Override
    public void onEditClick(PantryItem item) {

        Intent intent = new Intent(this, AddIngredientActivity.class);
        intent.putExtra("id", item.getId());
        intent.putExtra("name", item.getName());
        intent.putExtra("quantity", item.getQuantity());
        intent.putExtra("expiry", item.getExpiry());

        startActivity(intent);
    }

    @Override
    public void onDeleteClick(PantryItem item) {

        databaseHelper.deletePantryItem(item.getId());
        loadPantryItems();
    }
}