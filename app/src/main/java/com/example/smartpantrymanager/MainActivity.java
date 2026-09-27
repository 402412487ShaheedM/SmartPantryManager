package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
    private ArrayList<PantryItem> displayedItems = new ArrayList<>();

    private Button buttonAdd, buttonAll, buttonLow, buttonExpiring, buttonExpired;
    private EditText editSearch;
    private Spinner spinnerSort;

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
        spinnerSort = findViewById(R.id.spinnerSort);

        textTotalItems = findViewById(R.id.textTotalItems);
        textLowStockCount = findViewById(R.id.textLowStockCount);
        textExpiringCount = findViewById(R.id.textExpiringCount);
        textExpiredCount = findViewById(R.id.textExpiredCount);

        String[] options = {"Expiry Date", "Name (A-Z)", "Category"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, options);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                sortDisplayedItems();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        buttonAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddIngredientActivity.class)));

        buttonAll.setOnClickListener(v -> {
            displayedItems = new ArrayList<>(pantryItems);
            sortDisplayedItems();
        });

        buttonLow.setOnClickListener(v -> filterLowStock());
        buttonExpiring.setOnClickListener(v -> filterExpiring());
        buttonExpired.setOnClickListener(v -> filterExpired());

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterSearch(s.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        pantryItems = databaseHelper.getAllPantryItems();
        displayedItems = new ArrayList<>(pantryItems);
        sortDisplayedItems();
        updateStatistics();
    }

    private void refreshRecycler() {
        pantryAdapter = new PantryAdapter(displayedItems, this);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    private void sortDisplayedItems() {

        String option = spinnerSort.getSelectedItem().toString();

        switch (option) {

            case "Name (A-Z)":
                Collections.sort(displayedItems,
                        Comparator.comparing(PantryItem::getName, String.CASE_INSENSITIVE_ORDER));
                break;

            case "Category":
                Collections.sort(displayedItems,
                        Comparator.comparing(PantryItem::getCategory, String.CASE_INSENSITIVE_ORDER));
                break;

            default:
                Collections.sort(displayedItems, (a, b) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        return sdf.parse(a.getExpiry()).compareTo(sdf.parse(b.getExpiry()));
                    } catch (Exception e) {
                        return 0;
                    }
                });
        }

        refreshRecycler();
    }

    private void filterSearch(String text) {
        displayedItems.clear();

        for (PantryItem item : pantryItems) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                displayedItems.add(item);
            }
        }

        sortDisplayedItems();
    }

    private void filterLowStock() {
        displayedItems.clear();

        for (PantryItem item : pantryItems) {
            if (extractQuantity(item.getQuantity()) <= 5)
                displayedItems.add(item);
        }

        sortDisplayedItems();
    }

    private void filterExpiring() {
        displayedItems.clear();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date();

            for (PantryItem item : pantryItems) {
                Date expiry = sdf.parse(item.getExpiry());
                long days = TimeUnit.MILLISECONDS.toDays(expiry.getTime() - today.getTime());

                if (days >= 0 && days <= 7)
                    displayedItems.add(item);
            }
        } catch (Exception ignored) {}

        sortDisplayedItems();
    }

    private void filterExpired() {
        displayedItems.clear();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date();

            for (PantryItem item : pantryItems) {
                if (sdf.parse(item.getExpiry()).before(today))
                    displayedItems.add(item);
            }
        } catch (Exception ignored) {}

        sortDisplayedItems();
    }

    private void updateStatistics() {

        int total = pantryItems.size();
        int low = 0, expiring = 0, expired = 0;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date today = new Date();

            for (PantryItem item : pantryItems) {

                if (extractQuantity(item.getQuantity()) <= 5)
                    low++;

                Date expiry = sdf.parse(item.getExpiry());
                long days = TimeUnit.MILLISECONDS.toDays(expiry.getTime() - today.getTime());

                if (days < 0)
                    expired++;
                else if (days <= 7)
                    expiring++;
            }
        } catch (Exception ignored) {}

        textTotalItems.setText(String.valueOf(total));
        textLowStockCount.setText(String.valueOf(low));
        textExpiringCount.setText(String.valueOf(expiring));
        textExpiredCount.setText(String.valueOf(expired));
    }

    private int extractQuantity(String quantity) {
        String number = quantity.replaceAll("[^0-9]", "");
        return number.isEmpty() ? 0 : Integer.parseInt(number);
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