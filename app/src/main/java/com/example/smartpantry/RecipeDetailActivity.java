package com.example.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/** Shows the full ingredient list and method for the recipe passed in through the Intent. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        InsetsUtil.apply(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long id = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe r = new DatabaseHelper(this).getRecipe(id);
        if (r == null) {
            finish();
            return;
        }
        getSupportActionBar().setTitle(r.name);

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : r.ingredients) {
            sb.append("- ").append(UnitUtil.formatQty(ing.quantity)).append(" ")
              .append(ing.unit).append(" ").append(ing.name).append("\n");
        }
        ((TextView) findViewById(R.id.tv_ingredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tv_steps)).setText(r.steps);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
