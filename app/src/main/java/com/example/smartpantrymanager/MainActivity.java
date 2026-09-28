package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
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
    private ArrayList<PantryItem> displayList = new ArrayList<>();

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

        pantryAdapter = new PantryAdapter(displayList, this);
        recyclerViewPantry.setAdapter(pantryAdapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Name (A-Z)", "Expiry Date", "Category"});
        spinnerSort.setAdapter(sortAdapter);

        buttonAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddIngredientActivity.class)));

        buttonAll.setOnClickListener(v -> applyFilters("ALL"));
        buttonLow.setOnClickListener(v -> applyFilters("LOW"));
        buttonExpiring.setOnClickListener(v -> applyFilters("EXPIRING"));
        buttonExpired.setOnClickListener(v -> applyFilters("EXPIRED"));

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(currentFilter);
            }
        });

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applySorting();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private String currentFilter = "ALL";

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        pantryItems = databaseHelper.getAllPantryItems();
        updateStatistics();
        applyFilters(currentFilter);
    }

    private void applyFilters(String filter) {

        currentFilter = filter;
        displayList.clear();

        String search = editSearch.getText().toString().toLowerCase().trim();

        for (PantryItem item : pantryItems) {

            boolean matchesSearch = item.getName().toLowerCase().contains(search)
                    || item.getCategory().toLowerCase().contains(search);

            if (!matchesSearch) continue;

            switch (filter) {
                case "LOW":
                    if (extractQuantity(item.getQuantity()) <= 5) displayList.add(item);
                    break;

                case "EXPIRING":
                    long days = getDaysRemaining(item.getExpiry());
                    if (days >= 0 && days <= 7) displayList.add(item);
                    break;

                case "EXPIRED":
                    if (getDaysRemaining(item.getExpiry()) < 0) displayList.add(item);
                    break;

                default:
                    displayList.add(item);
            }
        }

        applySorting();
    }

    private void applySorting() {

        if (spinnerSort.getSelectedItem() == null) {
            pantryAdapter.notifyDataSetChanged();
            return;
        }

        String option = spinnerSort.getSelectedItem().toString();

        switch (option) {
            case "Expiry Date":
                Collections.sort(displayList, Comparator.comparing(PantryItem::getExpiry));
                break;

            case "Category":
                Collections.sort(displayList, Comparator.comparing(PantryItem::getCategory));
                break;

            default:
                Collections.sort(displayList, Comparator.comparing(PantryItem::getName));
        }

        pantryAdapter.notifyDataSetChanged();
    }

    private void updateStatistics() {

        int total = pantryItems.size();
        int low = 0;
        int expiring = 0;
        int expired = 0;

        for (PantryItem item : pantryItems) {

            if (extractQuantity(item.getQuantity()) <= 5) low++;

            long days = getDaysRemaining(item.getExpiry());

            if (days < 0) expired++;
            else if (days <= 7) expiring++;
        }

        textTotalItems.setText(String.valueOf(total));
        textLowStockCount.setText(String.valueOf(low));
        textExpiringCount.setText(String.valueOf(expiring));
        textExpiredCount.setText(String.valueOf(expired));
    }

    private long getDaysRemaining(String expiry) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date expiryDate = sdf.parse(expiry);
            return TimeUnit.MILLISECONDS.toDays(expiryDate.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            return 999;
        }
    }

    private int extractQuantity(String quantity) {
        String number = quantity.replaceAll("[^0-9]", "");
        if (number.isEmpty()) return 0;
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