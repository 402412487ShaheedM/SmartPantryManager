package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemClickListener {

    private RecyclerView recyclerViewPantry;
    private PantryAdapter pantryAdapter;
    private DatabaseHelper databaseHelper;

    private ArrayList<PantryItem> pantryItems = new ArrayList<>();
    private ArrayList<PantryItem> filteredItems = new ArrayList<>();

    private Button buttonAdd, buttonExport;
    private Button buttonAll, buttonLow, buttonExpiring, buttonExpired;

    private EditText editSearch;
    private Spinner spinnerSort;

    private TextView textTotalItems, textLowStockCount, textExpiringCount, textExpiredCount;

    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);

        buttonAdd = findViewById(R.id.buttonAdd);
        buttonExport = findViewById(R.id.buttonExport);

        buttonAll = findViewById(R.id.buttonAll);
        buttonLow = findViewById(R.id.buttonLow);
        buttonExpiring = findViewById(R.id.buttonExpiring);
        buttonExpired = findViewById(R.id.buttonExpired);

        editSearch = findViewById(R.id.editSearch);
        spinnerSort = findViewById(R.id.spinnerSort);

        textTotalItems = findViewById(R.id.textTotalItems);
        textLowStockCount = findViewById(R.id.textLowStockCount);
        textExpiringCount = findViewById(R.id.textExpiringCount);
        textExpiredCount = findViewById(R.id.textExpiredCount);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Name (A-Z)", "Expiry Date", "Category"}
        );
        spinnerSort.setAdapter(sortAdapter);

        buttonAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddIngredientActivity.class)));

        buttonExport.setOnClickListener(v -> exportPantryList());

        buttonAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            applyFilters();
        });

        buttonLow.setOnClickListener(v -> {
            currentFilter = "LOW";
            applyFilters();
        });

        buttonExpiring.setOnClickListener(v -> {
            currentFilter = "EXPIRING";
            applyFilters();
        });

        buttonExpired.setOnClickListener(v -> {
            currentFilter = "EXPIRED";
            applyFilters();
        });

        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
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
        updateStatistics();
        applyFilters();
        showRestockReminder();
    }

    private void applyFilters() {

        filteredItems.clear();

        String search = editSearch.getText().toString().toLowerCase().trim();

        for (PantryItem item : pantryItems) {

            boolean matchesSearch =
                    item.getName().toLowerCase().contains(search) ||
                            item.getCategory().toLowerCase().contains(search);

            if (!matchesSearch)
                continue;

            boolean include = true;

            switch (currentFilter) {

                case "LOW":
                    include = extractQuantity(item.getQuantity()) <= 5;
                    break;

                case "EXPIRING":
                    long days = getDaysRemaining(item.getExpiry());
                    include = days >= 0 && days <= 7;
                    break;

                case "EXPIRED":
                    include = getDaysRemaining(item.getExpiry()) < 0;
                    break;
            }

            if (include)
                filteredItems.add(item);
        }

        sortItems();

        pantryAdapter = new PantryAdapter(filteredItems, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void sortItems() {

        String option = spinnerSort.getSelectedItem().toString();

        switch (option) {

            case "Expiry Date":
                Collections.sort(filteredItems, Comparator.comparing(PantryItem::getExpiry));
                break;

            case "Category":
                Collections.sort(filteredItems, Comparator.comparing(PantryItem::getCategory));
                break;

            default:
                Collections.sort(filteredItems, Comparator.comparing(PantryItem::getName));
        }
    }

    private void updateStatistics() {

        int total = pantryItems.size();
        int low = 0;
        int expiring = 0;
        int expired = 0;

        for (PantryItem item : pantryItems) {

            if (extractQuantity(item.getQuantity()) <= 5)
                low++;

            long days = getDaysRemaining(item.getExpiry());

            if (days < 0)
                expired++;
            else if (days <= 7)
                expiring++;
        }

        textTotalItems.setText(String.valueOf(total));
        textLowStockCount.setText(String.valueOf(low));
        textExpiringCount.setText(String.valueOf(expiring));
        textExpiredCount.setText(String.valueOf(expired));
    }

    private void showRestockReminder() {

        int low = 0;

        for (PantryItem item : pantryItems) {
            if (extractQuantity(item.getQuantity()) <= 5)
                low++;
        }

        if (low == 0)
            return;

        new AlertDialog.Builder(this)
                .setTitle("Restock Reminder")
                .setMessage("You have " + low + " low stock items.\n\nConsider restocking your pantry.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void exportPantryList() {

        StringBuilder report = new StringBuilder();

        report.append("SHAHEED'S PANTRY REPORT\n");
        report.append("========================\n\n");

        report.append("Total Items: ").append(pantryItems.size()).append("\n\n");

        for (PantryItem item : pantryItems) {

            report.append(item.getName()).append("\n");
            report.append("Category: ").append(item.getCategory()).append("\n");
            report.append("Quantity: ").append(item.getQuantity()).append("\n");
            report.append("Expiry: ").append(item.getExpiry()).append("\n\n");
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "Pantry Report");
        share.putExtra(Intent.EXTRA_TEXT, report.toString());

        startActivity(Intent.createChooser(share, "Share Pantry Report"));
    }

    private long getDaysRemaining(String expiry) {

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date expiryDate = sdf.parse(expiry);

            long diff = expiryDate.getTime() - System.currentTimeMillis();

            return TimeUnit.MILLISECONDS.toDays(diff);

        } catch (Exception e) {
            return 999;
        }
    }

    private int extractQuantity(String quantity) {

        String number = quantity.replaceAll("[^0-9]", "");

        if (number.isEmpty())
            return 0;

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

        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deletePantryItem(item.getId());
                    loadPantryItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}