
package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddIngredientActivity extends AppCompatActivity {

    private EditText editName, editQuantity, editExpiry;
    private Spinner spinnerCategory;
    private Button buttonSave;

    private DatabaseHelper databaseHelper;

    private int itemId = -1;

    private final String[] categories = {
            "Grains",
            "Dairy",
            "Beverages",
            "Vegetables",
            "Spices",
            "Canned Food",
            "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        buttonSave = findViewById(R.id.buttonSave);

        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories);

        spinnerCategory.setAdapter(adapter);

        editExpiry.setOnClickListener(v -> showDatePicker());

        if (getIntent().hasExtra("id")) {

            itemId = getIntent().getIntExtra("id", -1);

            editName.setText(getIntent().getStringExtra("name"));
            editQuantity.setText(getIntent().getStringExtra("quantity"));
            editExpiry.setText(getIntent().getStringExtra("expiry"));

            String category = getIntent().getStringExtra("category");

            for (int i = 0; i < categories.length; i++) {

                if (categories[i].equals(category)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        buttonSave.setOnClickListener(v -> {

            PantryItem item = new PantryItem();

            item.setName(editName.getText().toString());
            item.setQuantity(editQuantity.getText().toString());
            item.setExpiry(editExpiry.getText().toString());
            item.setCategory(spinnerCategory.getSelectedItem().toString());

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
                            year);

                    editExpiry.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        picker.show();
    }
}