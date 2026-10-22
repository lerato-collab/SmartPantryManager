package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.material.appbar.MaterialToolbar;

/** Settings: expiring-soon alerts toggle and default unit, stored in SharedPreferences. */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        InsetsUtil.apply(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        setupBottomNav(R.id.nav_settings);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        Switch swAlerts = findViewById(R.id.sw_alerts);
        swAlerts.setChecked(prefs.getBoolean("expiry_alerts", true));
        swAlerts.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("expiry_alerts", checked).apply());

        Spinner spUnit = findViewById(R.id.sp_default_unit);
        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UnitUtil.UNITS));
        String current = prefs.getString("default_unit", "pcs");
        for (int i = 0; i < UnitUtil.UNITS.length; i++) {
            if (UnitUtil.UNITS[i].equals(current)) spUnit.setSelection(i);
        }
        spUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                prefs.edit().putString("default_unit", UnitUtil.UNITS[pos]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
