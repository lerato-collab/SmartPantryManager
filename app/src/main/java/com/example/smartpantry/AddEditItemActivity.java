package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.Locale;

/** Add or edit a single pantry ingredient, with input validation. */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private EditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private DatabaseHelper db;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        InsetsUtil.apply(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseHelper(this);
        etName = findViewById(R.id.et_name);
        etQty = findViewById(R.id.et_qty);
        etExpiry = findViewById(R.id.et_expiry);
        spUnit = findViewById(R.id.sp_unit);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnClear = findViewById(R.id.btn_clear_date);

        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UnitUtil.UNITS));

        // Preselect the unit chosen on the settings screen.
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        setUnitSelection(prefs.getString("default_unit", "pcs"));

        // An item id arrives via the Intent when editing; otherwise we are adding.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId >= 0) {
            getSupportActionBar().setTitle("Edit Ingredient");
            PantryItem item = db.getItem(itemId);
            if (item != null) {
                etName.setText(item.name);
                etQty.setText(UnitUtil.formatQty(item.quantity));
                etExpiry.setText(item.expiry);
                setUnitSelection(item.unit);
            }
        } else {
            getSupportActionBar().setTitle("Add Ingredient");
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnClear.setOnClickListener(v -> etExpiry.setText(""));
        btnSave.setOnClickListener(v -> save());
    }

    private void setUnitSelection(String unit) {
        for (int i = 0; i < UnitUtil.UNITS.length; i++) {
            if (UnitUtil.UNITS[i].equals(unit)) {
                spUnit.setSelection(i);
                return;
            }
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) ->
                etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String qtyText = etQty.getText().toString().trim();

        // Input validation: stop here and show an error on the field that is wrong.
        if (name.isEmpty()) {
            etName.setError("Please enter an ingredient name");
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            etQty.setError("Enter a valid number");
            return;
        }
        if (qty <= 0) {
            etQty.setError("Quantity must be greater than zero");
            return;
        }

        String unit = spUnit.getSelectedItem().toString();
        String expiry = etExpiry.getText().toString().trim();

        if (itemId >= 0) {
            db.updateItem(new PantryItem(itemId, name, qty, unit, expiry));
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        } else {
            db.insertItem(new PantryItem(0, name, qty, unit, expiry));
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
