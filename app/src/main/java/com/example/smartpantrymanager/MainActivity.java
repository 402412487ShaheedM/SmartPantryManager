package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements PantryAdapter.OnItemClickListener {

    private RecyclerView recyclerViewPantry;
    private PantryAdapter pantryAdapter;
    private DatabaseHelper databaseHelper;

    private ArrayList<PantryItem> pantryItems = new ArrayList<>();

    private Button buttonAdd, buttonExport, buttonRecipes;
    private Button buttonAll, buttonLow, buttonExpiring, buttonExpired;

    private EditText editSearch;
    private Spinner spinnerSort;
    private LinearLayout layoutEmpty;

    private TextView textTotalItems, textLowStockCount;
    private TextView textExpiringCount, textExpiredCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);

        buttonAdd = findViewById(R.id.buttonAdd);
        buttonExport = findViewById(R.id.buttonExport);
        buttonRecipes = findViewById(R.id.buttonRecipes);

        buttonAll = findViewById(R.id.buttonAll);
        buttonLow = findViewById(R.id.buttonLow);
        buttonExpiring = findViewById(R.id.buttonExpiring);
        buttonExpired = findViewById(R.id.buttonExpired);

        editSearch = findViewById(R.id.editSearch);
        spinnerSort = findViewById(R.id.spinnerSort);

        layoutEmpty = findViewById(R.id.layoutEmpty);

        textTotalItems = findViewById(R.id.textTotalItems);
        textLowStockCount = findViewById(R.id.textLowStockCount);
        textExpiringCount = findViewById(R.id.textExpiringCount);
        textExpiredCount = findViewById(R.id.textExpiredCount);

        String[] sortOptions = {
                "Name (A-Z)",
                "Expiry Date",
                "Category"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                sortOptions
        );

        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               android.view.View view,
                                               int position,
                                               long id) {
                        sortItems(position);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                });

        buttonAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddIngredientActivity.class)));

        buttonExport.setOnClickListener(v -> exportCSV());

        buttonRecipes.setOnClickListener(v ->
                startActivity(new Intent(this, RecipeActivity.class)));

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
        checkEmptyState();
        checkLowStockReminder();
    }

    private void checkEmptyState() {
        if (pantryItems.isEmpty()) {
            layoutEmpty.setVisibility(android.view.View.VISIBLE);
            recyclerViewPantry.setVisibility(android.view.View.GONE);
        } else {
            layoutEmpty.setVisibility(android.view.View.GONE);
            recyclerViewPantry.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void showAllItems() {
        pantryAdapter = new PantryAdapter(pantryItems, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void filterSearch(String text) {

        ArrayList<PantryItem> filtered = new ArrayList<>();

        for (PantryItem item : pantryItems) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())
                    || item.getCategory().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(item);
            }
        }

        pantryAdapter = new PantryAdapter(filtered, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void filterLowStock() {

        ArrayList<PantryItem> filtered = new ArrayList<>();

        for (PantryItem item : pantryItems) {
            if (extractQuantity(item.getQuantity()) <= 5) {
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

                long days = TimeUnit.MILLISECONDS.toDays(
                        expiry.getTime() - today.getTime());

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

    private void sortItems(int option) {

        switch (option) {

            case 0:
                Collections.sort(pantryItems,
                        Comparator.comparing(PantryItem::getName));
                break;

            case 1:
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault());

                    Collections.sort(pantryItems,
                            (a, b) -> {
                                try {
                                    return sdf.parse(a.getExpiry())
                                            .compareTo(sdf.parse(b.getExpiry()));
                                } catch (Exception e) {
                                    return 0;
                                }
                            });

                } catch (Exception ignored) { }

                break;

            case 2:
                Collections.sort(pantryItems,
                        Comparator.comparing(PantryItem::getCategory));
                break;
        }

        showAllItems();
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

                if (qty <= 5) low++;

                Date expiry = sdf.parse(item.getExpiry());

                long days = TimeUnit.MILLISECONDS.toDays(
                        expiry.getTime() - today.getTime());

                if (days < 0) expired++;
                else if (days <= 7) expiring++;
            }

        } catch (Exception ignored) { }

        textTotalItems.setText(String.valueOf(total));
        textLowStockCount.setText(String.valueOf(low));
        textExpiringCount.setText(String.valueOf(expiring));
        textExpiredCount.setText(String.valueOf(expired));
    }

    private void checkLowStockReminder() {

        int low = 0;

        for (PantryItem item : pantryItems) {
            if (extractQuantity(item.getQuantity()) <= 5) {
                low++;
            }
        }

        if (low > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Restock Reminder")
                    .setMessage("You have " + low +
                            " low stock items.\n\nConsider restocking your pantry.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void exportCSV() {

        try {

            File file = new File(getExternalFilesDir("Documents"),
                    "Pantry_Report.csv");

            FileWriter writer = new FileWriter(file);

            writer.append("Name,Category,Quantity,Expiry\n");

            for (PantryItem item : pantryItems) {
                writer.append(item.getName()).append(",");
                writer.append(item.getCategory()).append(",");
                writer.append(item.getQuantity()).append(",");
                writer.append(item.getExpiry()).append("\n");
            }

            writer.flush();
            writer.close();

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");

            share.putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".provider",
                            file
                    )
            );

            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(share, "Share Pantry Report"));

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete \"" +
                        item.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deletePantryItem(item.getId());
                    loadPantryItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}