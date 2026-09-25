package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddIngredientActivity extends AppCompatActivity {

    private AutoCompleteTextView editName;
    private EditText editQuantity, editExpiry;
    private Button buttonSave;

    private DatabaseHelper databaseHelper;

    private int itemId = -1;

    private final String[] suggestions = {
            "Rice",
            "Basmati Rice",
            "Brown Rice",
            "Bread",
            "Milk",
            "Sugar",
            "Coffee",
            "Tea",
            "Beans",
            "Pasta",
            "Cooking Oil",
            "Salt"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        buttonSave = findViewById(R.id.buttonSave);

        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                suggestions
        );

        editName.setAdapter(adapter);

        editExpiry.setOnClickListener(v -> showDatePicker());

        if (getIntent().hasExtra("id")) {

            itemId = getIntent().getIntExtra("id", -1);

            editName.setText(getIntent().getStringExtra("name"));
            editQuantity.setText(getIntent().getStringExtra("quantity"));
            editExpiry.setText(getIntent().getStringExtra("expiry"));

            buttonSave.setText("Update Ingredient");
        }

        buttonSave.setOnClickListener(v -> {

            String name = editName.getText().toString().trim();
            String quantity = editQuantity.getText().toString().trim();
            String expiry = editExpiry.getText().toString().trim();

            if (name.isEmpty() || quantity.isEmpty() || expiry.isEmpty()) {
                return;
            }

            PantryItem item = new PantryItem(name, quantity, expiry);

            if (itemId == -1) {
                databaseHelper.insertPantryItem(item);
            } else {
                item.setId(itemId);
                databaseHelper.updatePantryItem(item);
            }

            finish();
        });
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    String date = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            day,
                            month + 1,
                            year
                    );

                    editExpiry.setText(date);

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        picker.show();
    }
}