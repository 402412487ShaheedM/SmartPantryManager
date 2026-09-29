package com.example.smartpantrymanager;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
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

        ArrayList<Recipe> recipes = new ArrayList<>();

        recipes.add(new Recipe(
                "Chicken Fried Rice",
                Arrays.asList("Chicken", "Rice", "Onion"),
                Arrays.asList(
                        "Cook the rice and let it cool.",
                        "Fry the onion until soft.",
                        "Add the chicken and cook completely.",
                        "Mix in the rice and stir for 3 minutes."
                )
        ));

        recipes.add(new Recipe(
                "Grilled Cheese Toast",
                Arrays.asList("Bread", "Cheese"),
                Arrays.asList(
                        "Butter two slices of bread.",
                        "Place cheese between the slices.",
                        "Toast in a pan until golden on both sides."
                )
        ));

        recipes.add(new Recipe(
                "Creamy Scrambled Eggs",
                Arrays.asList("Eggs", "Milk"),
                Arrays.asList(
                        "Whisk eggs with a splash of milk.",
                        "Cook over low heat while stirring gently.",
                        "Serve immediately."
                )
        ));

        recipes.add(new Recipe(
                "Tomato & Onion Salad",
                Arrays.asList("Tomato", "Onion"),
                Arrays.asList(
                        "Slice the tomatoes and onion.",
                        "Season with salt and pepper.",
                        "Mix and serve fresh."
                )
        ));

        recipes.add(new Recipe(
                "French Toast",
                Arrays.asList("Bread", "Eggs", "Milk"),
                Arrays.asList(
                        "Beat eggs and milk together.",
                        "Dip the bread into the mixture.",
                        "Fry until golden on both sides."
                )
        ));

        recipes.add(new Recipe(
                "Cheese Omelette",
                Arrays.asList("Eggs", "Cheese"),
                Arrays.asList(
                        "Beat the eggs well.",
                        "Pour into a hot pan.",
                        "Add cheese before folding the omelette."
                )
        ));

        for (Recipe recipe : recipes) {

            ArrayList<String> missing = new ArrayList<>();
            int matched = 0;

            for (String ingredient : recipe.ingredients) {

                boolean found = false;

                for (String pantry : pantryNames) {
                    if (pantry.contains(ingredient.toLowerCase())) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    matched++;
                } else {
                    missing.add(ingredient);
                }
            }

            int matchPercent = (matched * 100) / recipe.ingredients.size();

            addRecipe(
                    recipe.name,
                    joinIngredients(recipe.ingredients),
                    recipe.steps,
                    matchPercent,
                    missing
            );
        }
    }

    private void addRecipe(String title,
                           String ingredients,
                           List<String> steps,
                           int matchPercent,
                           List<String> missingIngredients) {

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

        TextView recipeMatch = new TextView(this);
        recipeMatch.setText("Pantry Match: " + matchPercent + "%");
        recipeMatch.setTextSize(14);
        recipeMatch.setTypeface(null, Typeface.BOLD);
        recipeMatch.setPadding(0, 10, 0, 0);

        if (matchPercent == 100) {
            recipeMatch.setTextColor(0xFF2E7D32);
        } else if (matchPercent >= 50) {
            recipeMatch.setTextColor(0xFFF57C00);
        } else {
            recipeMatch.setTextColor(0xFFD32F2F);
        }

        TextView missing = new TextView(this);

        if (missingIngredients.isEmpty()) {
            missing.setText("Missing: None");
            missing.setTextColor(0xFF2E7D32);
        } else {
            missing.setText("Missing: " + joinIngredients(missingIngredients));
            missing.setTextColor(0xFFD32F2F);
        }

        missing.setTextSize(14);
        missing.setPadding(0, 6, 0, 0);

        TextView tapHint = new TextView(this);
        tapHint.setText("Tap to view cooking steps");
        tapHint.setTextSize(13);
        tapHint.setTextColor(0xFF757575);
        tapHint.setPadding(0, 12, 0, 0);

        TextView recipeSteps = new TextView(this);
        recipeSteps.setVisibility(View.GONE);
        recipeSteps.setTextSize(15);
        recipeSteps.setPadding(0, 18, 0, 0);

        StringBuilder builder = new StringBuilder("Method:\n\n");

        for (int i = 0; i < steps.size(); i++) {
            builder.append(i + 1)
                    .append(". ")
                    .append(steps.get(i))
                    .append("\n");
        }

        recipeSteps.setText(builder.toString());

        card.setOnClickListener(v -> {
            if (recipeSteps.getVisibility() == View.GONE) {
                recipeSteps.setVisibility(View.VISIBLE);
                tapHint.setText("Tap to hide cooking steps");
            } else {
                recipeSteps.setVisibility(View.GONE);
                tapHint.setText("Tap to view cooking steps");
            }
        });

        card.addView(recipeTitle);
        card.addView(recipeIngredients);
        card.addView(recipeMatch);
        card.addView(missing);
        card.addView(tapHint);
        card.addView(recipeSteps);

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
        List<String> steps;

        Recipe(String name, List<String> ingredients, List<String> steps) {
            this.name = name;
            this.ingredients = ingredients;
            this.steps = steps;
        }
    }
}