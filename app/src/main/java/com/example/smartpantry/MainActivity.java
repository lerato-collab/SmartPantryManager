package com.example.smartpantry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Pantry list screen: shows every ingredient and lets the user add, edit or delete them. */
public class MainActivity extends BaseActivity {

    private DatabaseHelper db;
    private RecyclerView rv;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InsetsUtil.apply(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Pantry");

        db = new DatabaseHelper(this);
        rv = findViewById(R.id.rv_pantry);
        tvEmpty = findViewById(R.id.tv_empty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems(); // refresh whenever we come back from the add/edit screen
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllItems();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean alerts = prefs.getBoolean("expiry_alerts", true);

        rv.setAdapter(new PantryAdapter(items, alerts, new PantryAdapter.Listener() {
            @Override
            public void onEdit(PantryItem item) {
                Intent i = new Intent(MainActivity.this, AddEditItemActivity.class);
                i.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.id);
                startActivity(i);
            }

            @Override
            public void onDelete(PantryItem item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete ingredient")
                        .setMessage("Remove " + item.name + " from your pantry?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deleteItem(item.id);
                            loadItems();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }));
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
