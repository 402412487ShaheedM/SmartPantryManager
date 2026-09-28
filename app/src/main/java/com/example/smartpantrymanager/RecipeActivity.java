package com.example.smartpantrymanager;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout recipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        databaseHelper = new DatabaseHelper(this);
        recipeContainer = findViewById(R.id.recipeContainer);

        loadRecipes();
    }

    private void loadRecipes() {

        ArrayList<PantryItem> pantryItems = databaseHelper.getAllPantryItems();
        ArrayList<String> pantryNames = new ArrayList<>();

        for (PantryItem item : pantryItems) {
            pantryNames.add(item.getName().toLowerCase());
        }

        ArrayList<Recipe> matchedRecipes = new ArrayList<>();

        ArrayList<Recipe> recipes = new ArrayList<>();

        recipes.add(new Recipe(
                "Chicken Fried Rice",
                Arrays.asList("Chicken", "Rice", "Onion")
        ));

        recipes.add(new Recipe(
                "Grilled Cheese Toast",
                Arrays.asList("Bread", "Cheese")
        ));

        recipes.add(new Recipe(
                "Creamy Scrambled Eggs",
                Arrays.asList("Eggs", "Milk")
        ));

        recipes.add(new Recipe(
                "Tomato & Onion Salad",
                Arrays.asList("Tomato", "Onion")
        ));

        recipes.add(new Recipe(
                "French Toast",
                Arrays.asList("Bread", "Eggs", "Milk")
        ));

        recipes.add(new Recipe(
                "Cheese Omelette",
                Arrays.asList("Eggs", "Cheese")
        ));

        for (Recipe recipe : recipes) {

            boolean canMake = true;

            for (String ingredient : recipe.ingredients) {

                boolean found = false;

                for (String pantry : pantryNames) {
                    if (pantry.contains(ingredient.toLowerCase())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    canMake = false;
                    break;
                }
            }

            if (canMake) {
                matchedRecipes.add(recipe);
            }
        }

        if (matchedRecipes.isEmpty()) {
            addRecipe(
                    "No recipes available",
                    "Add more pantry ingredients to unlock recipe suggestions."
            );
        } else {
            for (Recipe recipe : matchedRecipes) {
                addRecipe(recipe.name, joinIngredients(recipe.ingredients));
            }
        }
    }

    private void addRecipe(String title, String ingredients) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(30, 30, 30, 30);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);
        card.setBackgroundResource(R.drawable.pantry_item_background);

        TextView recipeTitle = new TextView(this);
        recipeTitle.setText(title);
        recipeTitle.setTextSize(20);
        recipeTitle.setTypeface(null, Typeface.BOLD);
        recipeTitle.setTextColor(getColor(R.color.primaryGreen));

        TextView recipeIngredients = new TextView(this);
        recipeIngredients.setText("Ingredients: " + ingredients);
        recipeIngredients.setTextSize(15);
        recipeIngredients.setPadding(0, 12, 0, 0);

        card.addView(recipeTitle);
        card.addView(recipeIngredients);

        recipeContainer.addView(card);
    }

    private String joinIngredients(List<String> ingredients) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < ingredients.size(); i++) {
            builder.append(ingredients.get(i));

            if (i < ingredients.size() - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    private static class Recipe {

        String name;
        List<String> ingredients;

        Recipe(String name, List<String> ingredients) {
            this.name = name;
            this.ingredients = ingredients;
        }
    }
}