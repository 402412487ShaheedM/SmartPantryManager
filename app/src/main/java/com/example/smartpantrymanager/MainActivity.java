package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements PantryAdapter.OnItemClickListener {

    private RecyclerView recyclerViewPantry;
    private Button buttonAdd;
    private EditText editSearch;

    private DatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;

    private ArrayList<PantryItem> pantryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);
        buttonAdd = findViewById(R.id.buttonAdd);
        editSearch = findViewById(R.id.editSearch);

        databaseHelper = new DatabaseHelper(this);

        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        loadPantryItems();

        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddIngredientActivity.class);
            startActivity(intent);
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
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