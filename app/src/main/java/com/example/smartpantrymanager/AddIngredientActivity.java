package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddIngredientActivity extends AppCompatActivity {

    private EditText editName, editQuantity, editExpiry;
    private Button buttonSave;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        buttonSave = findViewById(R.id.buttonSave);

        databaseHelper = new DatabaseHelper(this);

        buttonSave.setOnClickListener(v -> saveIngredient());
    }

    private void saveIngredient() {

        String name = editName.getText().toString().trim();
        String quantity = editQuantity.getText().toString().trim();
        String expiry = editExpiry.getText().toString().trim();

        if (name.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(this,
                    "Please enter ingredient name and quantity",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        PantryItem item = new PantryItem(name, quantity, expiry);

        databaseHelper.insertPantryItem(item);

        Toast.makeText(this,
                "Ingredient saved",
                Toast.LENGTH_SHORT).show();

        finish();
    }
}