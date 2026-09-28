package com.example.smartpantrymanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private PantryAdapter adapter;
    private DatabaseHelper databaseHelper;

    private ArrayList<PantryItem> pantryList = new ArrayList<>();
    private ArrayList<PantryItem> filteredList = new ArrayList<>();

    private EditText editSearch;
    private Spinner spinnerSort;

    private Button btnAll, btnLow, btnExpiring, btnExpired, btnAdd, btnExport;

    private TextView txtTotalItems, txtLowStock, txtExpiring, txtExpired;

    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        editSearch = findViewById(R.id.editSearch);
        spinnerSort = findViewById(R.id.spinnerSort);

        btnAll = findViewById(R.id.buttonAll);
        btnLow = findViewById(R.id.buttonLow);
        btnExpiring = findViewById(R.id.buttonExpiring);
        btnExpired = findViewById(R.id.buttonExpired);
        btnAdd = findViewById(R.id.buttonAdd);
        btnExport = findViewById(R.id.buttonExport);

        txtTotalItems = findViewById(R.id.textTotalItems);
        txtLowStock = findViewById(R.id.textLowStockCount);
        txtExpiring = findViewById(R.id.textExpiringCount);
        txtExpired = findViewById(R.id.textExpiredCount);

        adapter = new PantryAdapter(filteredList, this);
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        "Name (A-Z)",
                        "Expiry Date",
                        "Category"
                });

        spinnerSort.setAdapter(sortAdapter);

        loadPantryItems();
        showLowStockReminder();

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddIngredientActivity.class)));

        btnExport.setOnClickListener(v -> exportCSV());

        btnAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            applyFilters();
        });

        btnLow.setOnClickListener(v -> {
            currentFilter = "LOW";
            applyFilters();
        });

        btnExpiring.setOnClickListener(v -> {
            currentFilter = "EXPIRING";
            applyFilters();
        });

        btnExpired.setOnClickListener(v -> {
            currentFilter = "EXPIRED";
            applyFilters();
        });

        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        pantryList = databaseHelper.getAllPantryItems();
        updateStatistics();
        applyFilters();
    }

    private void applyFilters() {

        filteredList.clear();

        String search = editSearch.getText().toString().toLowerCase().trim();

        for (PantryItem item : pantryList) {

            boolean matchesSearch =
                    item.getName().toLowerCase().contains(search) ||
                            item.getCategory().toLowerCase().contains(search);

            if (!matchesSearch)
                continue;

            boolean include;

            switch (currentFilter) {

                case "LOW":
                    include = extractQuantity(item.getQuantity()) <= 5;
                    break;

                case "EXPIRING":
                    include = getDaysRemaining(item.getExpiry()) >= 0 &&
                            getDaysRemaining(item.getExpiry()) <= 7;
                    break;

                case "EXPIRED":
                    include = getDaysRemaining(item.getExpiry()) < 0;
                    break;

                default:
                    include = true;
            }

            if (include)
                filteredList.add(item);
        }

        sortItems();

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            findViewById(R.id.layoutEmpty).setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.layoutEmpty).setVisibility(View.GONE);
        }
    }

    private void sortItems() {

        String selected = spinnerSort.getSelectedItem().toString();

        switch (selected) {

            case "Expiry Date":
                Collections.sort(filteredList,
                        Comparator.comparing(PantryItem::getExpiry));
                break;

            case "Category":
                Collections.sort(filteredList,
                        Comparator.comparing(PantryItem::getCategory));
                break;

            default:
                Collections.sort(filteredList,
                        Comparator.comparing(PantryItem::getName));
        }
    }

    private void updateStatistics() {

        int total = pantryList.size();
        int low = 0;
        int expiring = 0;
        int expired = 0;

        for (PantryItem item : pantryList) {

            if (extractQuantity(item.getQuantity()) <= 5)
                low++;

            long days = getDaysRemaining(item.getExpiry());

            if (days < 0)
                expired++;
            else if (days <= 7)
                expiring++;
        }

        txtTotalItems.setText(String.valueOf(total));
        txtLowStock.setText(String.valueOf(low));
        txtExpiring.setText(String.valueOf(expiring));
        txtExpired.setText(String.valueOf(expired));
    }

    private void showLowStockReminder() {

        int low = 0;

        for (PantryItem item : pantryList) {
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

    private int extractQuantity(String quantity) {

        String number = quantity.replaceAll("[^0-9]", "");

        if (number.isEmpty())
            return 0;

        return Integer.parseInt(number);
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

    private void exportCSV() {

        try {

            File folder = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

            if (folder != null && !folder.exists())
                folder.mkdirs();

            File file = new File(folder, "Pantry_Report.csv");

            FileWriter writer = new FileWriter(file);

            writer.append("Name,Category,Quantity,Expiry\n");

            for (PantryItem item : pantryList) {

                writer.append(item.getName()).append(",");
                writer.append(item.getCategory()).append(",");
                writer.append(item.getQuantity()).append(",");
                writer.append(item.getExpiry()).append("\n");
            }

            writer.flush();
            writer.close();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(share, "Export Pantry Report"));

        } catch (Exception e) {

            new AlertDialog.Builder(this)
                    .setTitle("Export Failed")
                    .setMessage(e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }
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