package com.example.smartpantrymanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPantry;
    private PantryAdapter pantryAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to the database
        databaseHelper = new DatabaseHelper(this);

        // Find the RecyclerView on the screen
        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);

        // Display pantry items in a vertical list
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        // Get pantry items from SQLite
        ArrayList<PantryItem> pantryItems =
                databaseHelper.getAllPantryItems();

        // Connect the data to the RecyclerView
        pantryAdapter = new PantryAdapter(pantryItems, null);
        recyclerViewPantry.setAdapter(pantryAdapter);
    }
}