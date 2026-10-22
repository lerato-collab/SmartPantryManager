package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

/** Runs the strict-matching logic against the current pantry and lists the results. */
public class SuggestedRecipesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        InsetsUtil.apply(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Suggested Recipes");
        setupBottomNav(R.id.nav_suggested);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseHelper db = new DatabaseHelper(this);
        RecipeMatcher.Result result = RecipeMatcher.match(db.getAllItems(), db.getAllRecipes());

        RecyclerView rvStrict = findViewById(R.id.rv_strict);
        RecyclerView rvAlmost = findViewById(R.id.rv_almost);
        TextView tvEmpty = findViewById(R.id.tv_empty);
        TextView tvAlmostHeader = findViewById(R.id.tv_almost_header);

        RecipeAdapter.Listener open = recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(i);
        };

        rvStrict.setLayoutManager(new LinearLayoutManager(this));
        rvStrict.setNestedScrollingEnabled(false);
        rvStrict.setAdapter(new RecipeAdapter(result.strict, open));

        rvAlmost.setLayoutManager(new LinearLayoutManager(this));
        rvAlmost.setNestedScrollingEnabled(false);
        rvAlmost.setAdapter(new RecipeAdapter(result.almost, open));

        // Friendly message instead of a blank screen when nothing matches.
        tvEmpty.setVisibility(result.strict.isEmpty() ? View.VISIBLE : View.GONE);
        tvAlmostHeader.setVisibility(result.almost.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
