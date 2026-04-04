package com.example.istivat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private PreferencesHelper prefsHelper;
    private CalculatorFragment calculatorFragment;
    private HistoryFragment historyFragment;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PreferencesHelper.PREFS_NAME, MODE_PRIVATE);
        prefsHelper = new PreferencesHelper(prefs);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            int statusBarHeight = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            toolbar.setPadding(0, statusBarHeight, 0, 0);
            return windowInsets;
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        bottomNav = findViewById(R.id.bottomNav);
        calculatorFragment = new CalculatorFragment();
        historyFragment = new HistoryFragment();

        if (savedInstanceState == null) {
            showFragment(calculatorFragment);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_calculator) {
                showFragment(calculatorFragment);
            } else if (item.getItemId() == R.id.nav_history) {
                showFragment(historyFragment);
            }
            return true;
        });
    }

    public void switchToCalculator() {
        bottomNav.setSelectedItemId(R.id.nav_calculator);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_change_language) { showLanguageDialog(); return true; }
        if (id == R.id.menu_about) { showAboutDialog(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void attachBaseContext(Context base) {
        SharedPreferences prefs = base.getSharedPreferences(PreferencesHelper.PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(PreferencesHelper.KEY_LANGUAGE, PreferencesHelper.LANGUAGE_CODES[0]);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        super.attachBaseContext(base.createConfigurationContext(config));
    }

    private void showLanguageDialog() {
        String[] languages = getResources().getStringArray(R.array.language_options_array);
        String current = prefsHelper.loadLanguage();
        int selectedIndex = Arrays.asList(PreferencesHelper.LANGUAGE_CODES).indexOf(current);
        if (selectedIndex < 0) selectedIndex = 0;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language_title)
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String code = PreferencesHelper.LANGUAGE_CODES[which];
                    if (!code.equals(current)) {
                        prefsHelper.saveLanguage(code);
                        recreate();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_about)
                .setMessage(R.string.about_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
