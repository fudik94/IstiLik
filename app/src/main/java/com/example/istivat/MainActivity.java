package com.example.istivat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "heating_prefs";
    private static final String KEY_AREA = "area";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_RADIATOR_POWER = "radiator_power";
    private static final String KEY_FLOOR_AREA = "floor_area";
    private static final String KEY_HOUSING = "housing";
    private static final String KEY_WALLS = "walls";
    private static final String KEY_INSULATION = "insulation";
    private static final String KEY_FLOOR = "floor";
    private static final String KEY_SYSTEM = "system";
    private static final String KEY_DHW = "dhw_points";
    private static final String KEY_RESULT_TEXT = "result_text";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_HISTORY = "history_entries";
    private static final String TAG = "MainActivity";

    private static final double MIN_AREA = 5.0;
    private static final double MAX_AREA = 1000.0;
    private static final double MIN_HEIGHT = 2.0;
    private static final double MAX_HEIGHT = 6.0;
    private static final double MIN_RADIATOR_POWER = 50.0;
    private static final double MAX_RADIATOR_POWER = 500.0;
    private static final double DEFAULT_RADIATOR_POWER = 180.0;
    private static final int MAX_DHW_POINTS = 9;
    private static final String HISTORY_DELIMITER = "|";
    private static final String[] LANGUAGE_CODES = {"az", "ru", "en"};
    private static final float PDF_TEXT_SIZE = 14f;
    private static final int PDF_PAGE_WIDTH = 595;
    private static final int PDF_PAGE_HEIGHT = 842;
    private static final int PDF_MARGIN_X = 40;
    private static final int PDF_START_Y = 60;
    private static final int PDF_LINE_HEIGHT = 22;

    private TextInputLayout layoutArea;
    private TextInputLayout layoutHeight;
    private TextInputLayout layoutRadiatorPower;
    private TextInputLayout layoutFloorArea;
    private TextInputLayout layoutSystem;
    private TextInputLayout layoutDhwPoints;
    private TextInputEditText editArea;
    private TextInputEditText editHeight;
    private TextInputEditText editRadiatorPower;
    private TextInputEditText editFloorArea;
    private MaterialAutoCompleteTextView autoHousing;
    private MaterialAutoCompleteTextView autoWalls;
    private MaterialAutoCompleteTextView autoInsulation;
    private MaterialAutoCompleteTextView autoFloor;
    private MaterialAutoCompleteTextView autoSystem;
    private MaterialAutoCompleteTextView autoDhwPoints;
    private MaterialButton btnCalculate;
    private MaterialButton btnReset;
    private MaterialButton btnShare;
    private TextView textResult;
    private SharedPreferences preferences;

    private int housingPosition;
    private int wallsPosition;
    private int insulationPosition;
    private int floorPosition;
    private int systemPosition;
    private int dhwPosition;
    private HeatingCalculator.Input lastInput;
    private HeatingCalculator.Result lastResult;
    private int lastHousingPosition = -1;
    private int lastInsulationPosition = -1;
    private int lastFloorPosition = -1;
    private int lastSystemPosition = -1;
    private int lastDhwPosition = -1;
    private boolean isRestoring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        applySavedLocale();
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layoutArea = findViewById(R.id.layoutArea);
        layoutHeight = findViewById(R.id.layoutHeight);
        layoutRadiatorPower = findViewById(R.id.layoutRadiatorPower);
        layoutFloorArea = findViewById(R.id.layoutFloorArea);
        layoutSystem = findViewById(R.id.layoutSystem);
        layoutDhwPoints = findViewById(R.id.layoutDhwPoints);

        editArea = findViewById(R.id.editArea);
        editHeight = findViewById(R.id.editHeight);
        editRadiatorPower = findViewById(R.id.editRadiatorPower);
        editFloorArea = findViewById(R.id.editFloorArea);

        autoHousing = findViewById(R.id.autoHousing);
        autoWalls = findViewById(R.id.autoWalls);
        autoInsulation = findViewById(R.id.autoInsulation);
        autoFloor = findViewById(R.id.autoFloor);
        autoSystem = findViewById(R.id.autoSystem);
        autoDhwPoints = findViewById(R.id.autoDhwPoints);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnReset = findViewById(R.id.btnReset);
        btnShare = findViewById(R.id.btnShare);
        textResult = findViewById(R.id.textResult);

        isRestoring = true;
        setupDropdown(autoHousing, R.array.housing_array, position -> housingPosition = position);
        setupDropdown(autoWalls, R.array.walls_array, position -> wallsPosition = position);
        setupDropdown(autoInsulation, R.array.insulation_array, position -> insulationPosition = position);
        setupDropdown(autoFloor, R.array.floor_array, position -> floorPosition = position);
        setupDropdown(autoSystem, R.array.system_array, position -> {
            systemPosition = position;
            updateSystemMode();
            if (!isRestoring) {
                clearResult();
            }
            validateForm();
        });
        setupDropdown(autoDhwPoints, R.array.dhw_points_array, position -> dhwPosition = position);

        setupValidation();
        restoreState();

        btnCalculate.setOnClickListener(v -> calculateHeating());
        btnReset.setOnClickListener(v -> resetForm());
        btnShare.setOnClickListener(v -> shareReport());
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveInputState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_change_language) {
            showLanguageDialog();
            return true;
        }
        if (itemId == R.id.menu_history) {
            showHistoryDialog();
            return true;
        }
        if (itemId == R.id.menu_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applySavedLocale() {
        String language = preferences.getString(KEY_LANGUAGE, LANGUAGE_CODES[0]);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(getResources().getConfiguration());
        configuration.setLocale(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    private void setupDropdown(MaterialAutoCompleteTextView view, int arrayResId, SelectionListener listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                getResources().getStringArray(arrayResId)
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        view.setAdapter(adapter);
        view.setOnItemClickListener((parent, v, position, id) -> listener.onSelected(position));
        listener.onSelected(0);
        view.setText(adapter.getItem(0), false);
    }

    private void updateSystemMode() {
        boolean underfloorOnly = isUnderfloorOnly();
        layoutRadiatorPower.setEnabled(!underfloorOnly);
        editRadiatorPower.setEnabled(!underfloorOnly);
        if (underfloorOnly) {
            layoutRadiatorPower.setError(null);
        }
    }

    private void restoreState() {
        isRestoring = true;
        editArea.setText(preferences.getString(KEY_AREA, ""));
        editHeight.setText(preferences.getString(KEY_HEIGHT, ""));
        String radiatorValue = preferences.getString(KEY_RADIATOR_POWER, null);
        if (radiatorValue == null || radiatorValue.isEmpty()) {
            radiatorValue = String.valueOf((int) DEFAULT_RADIATOR_POWER);
        }
        editRadiatorPower.setText(radiatorValue);
        editFloorArea.setText(preferences.getString(KEY_FLOOR_AREA, ""));

        setDropdownSelection(autoHousing, R.array.housing_array, preferences.getInt(KEY_HOUSING, 0),
                position -> housingPosition = position);
        setDropdownSelection(autoWalls, R.array.walls_array, preferences.getInt(KEY_WALLS, 0),
                position -> wallsPosition = position);
        setDropdownSelection(autoInsulation, R.array.insulation_array, preferences.getInt(KEY_INSULATION, 0),
                position -> insulationPosition = position);
        setDropdownSelection(autoFloor, R.array.floor_array, preferences.getInt(KEY_FLOOR, 0),
                position -> floorPosition = position);
        setDropdownSelection(autoSystem, R.array.system_array, preferences.getInt(KEY_SYSTEM, 0),
                position -> {
                    systemPosition = position;
                    updateSystemMode();
                });
        setDropdownSelection(autoDhwPoints, R.array.dhw_points_array, preferences.getInt(KEY_DHW, 0),
                position -> dhwPosition = position);

        String resultText = preferences.getString(KEY_RESULT_TEXT, null);
        if (resultText != null) {
            textResult.setText(resultText);
            if (validateForm()) {
                HeatingCalculator.Input input = buildInputFromFields();
                if (input != null) {
                    lastInput = input;
                    lastResult = HeatingCalculator.calculate(input);
                    lastHousingPosition = housingPosition;
                    lastInsulationPosition = insulationPosition;
                    lastFloorPosition = floorPosition;
                    lastSystemPosition = systemPosition;
                    lastDhwPosition = dhwPosition;
                }
            }
        } else {
            textResult.setText(R.string.result_placeholder);
        }

        btnShare.setEnabled(lastResult != null);
        validateForm();
        isRestoring = false;
    }

    private void setDropdownSelection(MaterialAutoCompleteTextView view,
                                      int arrayResId,
                                      int position,
                                      SelectionListener listener) {
        String[] entries = getResources().getStringArray(arrayResId);
        int safePosition = Math.min(Math.max(position, 0), entries.length - 1);
        listener.onSelected(safePosition);
        view.setText(entries[safePosition], false);
    }

    private void setupValidation() {
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        };
        editArea.addTextChangedListener(watcher);
        editHeight.addTextChangedListener(watcher);
        editRadiatorPower.addTextChangedListener(watcher);
        editFloorArea.addTextChangedListener(watcher);
    }

    private boolean validateForm() {
        boolean isValid = true;
        boolean underfloorOnly = isUnderfloorOnly();

        Double area = parseDouble(editArea);
        if (area == null) {
            layoutArea.setError(getString(R.string.error_required));
            isValid = false;
        } else if (area < MIN_AREA || area > MAX_AREA) {
            layoutArea.setError(getString(R.string.error_invalid_area));
            isValid = false;
        } else {
            layoutArea.setError(null);
        }

        Double height = parseDouble(editHeight);
        if (height == null) {
            layoutHeight.setError(getString(R.string.error_required));
            isValid = false;
        } else if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
            layoutHeight.setError(getString(R.string.error_invalid_height));
            isValid = false;
        } else {
            layoutHeight.setError(null);
        }

        Double radiatorPower = parseDouble(editRadiatorPower);
        if (underfloorOnly) {
            layoutRadiatorPower.setError(null);
        } else {
            if (radiatorPower == null) {
                layoutRadiatorPower.setError(getString(R.string.error_required));
                isValid = false;
            } else if (radiatorPower < MIN_RADIATOR_POWER || radiatorPower > MAX_RADIATOR_POWER) {
                layoutRadiatorPower.setError(getString(R.string.error_invalid_radiator));
                isValid = false;
            } else {
                layoutRadiatorPower.setError(null);
            }
        }

        Double floorArea = parseDouble(editFloorArea);
        if (floorArea == null) {
            layoutFloorArea.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            double maxFloorArea = area != null ? area : MAX_AREA;
            if (floorArea < 0 || (area != null && floorArea > area)) {
                layoutFloorArea.setError(getString(R.string.error_invalid_floor_area, maxFloorArea));
                isValid = false;
            } else {
                layoutFloorArea.setError(null);
            }
        }

        btnCalculate.setEnabled(isValid);
        return isValid;
    }

    private void calculateHeating() {
        if (!validateForm()) {
            Toast.makeText(this, getString(R.string.error_fill), Toast.LENGTH_SHORT).show();
            return;
        }
        HeatingCalculator.Input input = buildInputFromFields();
        if (input == null) {
            Toast.makeText(this, getString(R.string.error_fill), Toast.LENGTH_SHORT).show();
            return;
        }

        HeatingCalculator.Result result = HeatingCalculator.calculate(input);
        lastInput = input;
        lastResult = result;
        lastHousingPosition = housingPosition;
        lastInsulationPosition = insulationPosition;
        lastFloorPosition = floorPosition;
        lastSystemPosition = systemPosition;
        lastDhwPosition = dhwPosition;

        String resultText = buildResultText(input, result);
        textResult.setText(resultText);
        btnShare.setEnabled(true);

        saveInputState();
        preferences.edit().putString(KEY_RESULT_TEXT, resultText).apply();
        appendHistoryEntry(input, result);
    }

    private void resetForm() {
        editArea.setText("");
        editHeight.setText("");
        editRadiatorPower.setText(String.valueOf((int) DEFAULT_RADIATOR_POWER));
        editFloorArea.setText("");

        setDropdownSelection(autoHousing, R.array.housing_array, 0, position -> housingPosition = position);
        setDropdownSelection(autoWalls, R.array.walls_array, 0, position -> wallsPosition = position);
        setDropdownSelection(autoInsulation, R.array.insulation_array, 0, position -> insulationPosition = position);
        setDropdownSelection(autoFloor, R.array.floor_array, 0, position -> floorPosition = position);
        setDropdownSelection(autoSystem, R.array.system_array, 0, position -> {
            systemPosition = position;
            updateSystemMode();
        });
        setDropdownSelection(autoDhwPoints, R.array.dhw_points_array, 0, position -> dhwPosition = position);

        layoutArea.setError(null);
        layoutHeight.setError(null);
        layoutRadiatorPower.setError(null);
        layoutFloorArea.setError(null);

        clearResult();
        lastHousingPosition = -1;
        lastInsulationPosition = -1;
        lastFloorPosition = -1;
        lastSystemPosition = -1;
        lastDhwPosition = -1;

        saveInputState();
        validateForm();
    }

    private String buildResultText(HeatingCalculator.Input input, HeatingCalculator.Result result) {
        boolean underfloorOnly = input.systemType == HeatingCalculator.SystemType.UNDERFLOOR_ONLY;
        StringBuilder builder = new StringBuilder(getString(R.string.result_power, result.requiredPowerKw));
        if (!underfloorOnly) {
            builder.append("\n").append(getString(R.string.result_sections, result.radiatorSections));
        }
        builder.append("\n").append(getString(R.string.result_boiler, result.recommendedBoilerKw));
        return builder.toString();
    }

    private void clearResult() {
        textResult.setText(R.string.result_placeholder);
        lastInput = null;
        lastResult = null;
        btnShare.setEnabled(false);
        preferences.edit().remove(KEY_RESULT_TEXT).apply();
    }

    private HeatingCalculator.Input buildInputFromFields() {
        Double area = parseDouble(editArea);
        Double height = parseDouble(editHeight);
        Double radiatorPower = parseDouble(editRadiatorPower);
        Double floorArea = parseDouble(editFloorArea);

        if (area == null || height == null || floorArea == null) {
            return null;
        }
        if (isUnderfloorOnly()) {
            if (radiatorPower == null) {
                radiatorPower = DEFAULT_RADIATOR_POWER;
            }
        } else if (radiatorPower == null) {
            return null;
        }

        Integer wallCount = parseInteger(autoWalls.getText().toString());
        if (wallCount == null) {
            return null;
        }
        int safeDhwPosition = Math.min(Math.max(dhwPosition, 0), MAX_DHW_POINTS - 1);
        int dhwPoints = safeDhwPosition + 1;
        return new HeatingCalculator.Input(
                area,
                height,
                wallCount,
                radiatorPower,
                floorArea,
                housingPosition == 1 ? HeatingCalculator.HousingType.HOUSE : HeatingCalculator.HousingType.APARTMENT,
                mapInsulation(insulationPosition),
                floorPosition == 1 ? HeatingCalculator.FloorType.ATTIC : HeatingCalculator.FloorType.STANDARD,
                systemPosition == 1 ? HeatingCalculator.SystemType.UNDERFLOOR_ONLY : HeatingCalculator.SystemType.MIXED,
                dhwPoints
        );
    }

    private HeatingCalculator.InsulationLevel mapInsulation(int position) {
        switch (position) {
            case 0:
                return HeatingCalculator.InsulationLevel.GOOD;
            case 2:
                return HeatingCalculator.InsulationLevel.POOR;
            case 1:
            default:
                return HeatingCalculator.InsulationLevel.MEDIUM;
        }
    }

    private void saveInputState() {
        preferences.edit()
                .putString(KEY_AREA, valueOrEmpty(editArea))
                .putString(KEY_HEIGHT, valueOrEmpty(editHeight))
                .putString(KEY_RADIATOR_POWER, valueOrEmpty(editRadiatorPower))
                .putString(KEY_FLOOR_AREA, valueOrEmpty(editFloorArea))
                .putInt(KEY_HOUSING, housingPosition)
                .putInt(KEY_WALLS, wallsPosition)
                .putInt(KEY_INSULATION, insulationPosition)
                .putInt(KEY_FLOOR, floorPosition)
                .putInt(KEY_SYSTEM, systemPosition)
                .putInt(KEY_DHW, dhwPosition)
                .apply();
    }

    private void shareReport() {
        if (lastInput == null || lastResult == null) {
            Toast.makeText(this, getString(R.string.report_share_error), Toast.LENGTH_SHORT).show();
            return;
        }

        String reportText = buildReportText(lastInput, lastResult);
        try {
            File pdfFile = createPdfReport(reportText);
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    pdfFile
            );
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_share_message));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.report_share_title)));
        } catch (IOException | IllegalArgumentException exception) {
            Log.e(TAG, "Failed to share PDF report", exception);
            Toast.makeText(this, getString(R.string.report_share_error), Toast.LENGTH_SHORT).show();
        }
    }

    private String buildReportText(HeatingCalculator.Input input, HeatingCalculator.Result result) {
        String housing = resolveEntry(R.array.housing_array, lastHousingPosition, autoHousing.getText().toString());
        String insulation = resolveEntry(R.array.insulation_array, lastInsulationPosition, autoInsulation.getText().toString());
        String floor = resolveEntry(R.array.floor_array, lastFloorPosition, autoFloor.getText().toString());
        String system = resolveEntry(R.array.system_array, lastSystemPosition, autoSystem.getText().toString());
        String dhwPoints = resolveEntry(R.array.dhw_points_array, lastDhwPosition, autoDhwPoints.getText().toString());
        boolean underfloorOnly = input.systemType == HeatingCalculator.SystemType.UNDERFLOOR_ONLY;
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.report_title)).append("\n\n");
        builder.append(String.format(Locale.getDefault(), "%s: %.1f %s\n",
                getString(R.string.area), input.area, getString(R.string.unit_square_meter)));
        builder.append(String.format(Locale.getDefault(), "%s: %.1f %s\n",
                getString(R.string.height), input.height, getString(R.string.unit_meter)));
        builder.append(String.format(Locale.getDefault(), "%s: %s\n", getString(R.string.housing_type), housing));
        builder.append(String.format(Locale.getDefault(), "%s: %d\n", getString(R.string.walls), input.wallCount));
        builder.append(String.format(Locale.getDefault(), "%s: %s\n", getString(R.string.insulation), insulation));
        builder.append(String.format(Locale.getDefault(), "%s: %s\n", getString(R.string.floor_type), floor));
        builder.append(String.format(Locale.getDefault(), "%s: %s\n", getString(R.string.system_type), system));
        builder.append(String.format(Locale.getDefault(), "%s: %s\n", getString(R.string.dhw_points), dhwPoints));
        if (!underfloorOnly) {
            builder.append(String.format(Locale.getDefault(), "%s: %.1f %s\n",
                    getString(R.string.radiator_power), input.radiatorPower, getString(R.string.unit_watt)));
        }
        builder.append(String.format(Locale.getDefault(), "%s: %.1f %s\n\n",
                getString(R.string.floor_area), input.floorArea, getString(R.string.unit_square_meter)));
        builder.append(getString(R.string.result_power, result.requiredPowerKw)).append("\n");
        if (!underfloorOnly) {
            builder.append(getString(R.string.result_sections, result.radiatorSections)).append("\n");
        }
        builder.append(getString(R.string.result_boiler, result.recommendedBoilerKw));
        return builder.toString();
    }

    private void appendHistoryEntry(HeatingCalculator.Input input, HeatingCalculator.Result result) {
        String date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date());
        String area = String.format(Locale.getDefault(), "%.1f", input.area);
        String entry = date + HISTORY_DELIMITER + area + HISTORY_DELIMITER + result.recommendedBoilerKw;
        String existingHistory = preferences.getString(KEY_HISTORY, "");
        String updatedHistory = (existingHistory == null || existingHistory.trim().isEmpty())
                ? entry
                : entry + "\n" + existingHistory;
        preferences.edit().putString(KEY_HISTORY, updatedHistory).apply();
    }

    private void showHistoryDialog() {
        String history = preferences.getString(KEY_HISTORY, "");
        String[] items = buildHistoryItems(history);
        if (items.length == 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.menu_history)
                    .setMessage(R.string.history_empty)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_history)
                .setItems(items, null)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.clear_history, (dialog, which) -> clearHistory())
                .show();
    }

    private void clearHistory() {
        preferences.edit().remove(KEY_HISTORY).apply();
        Toast.makeText(this, getString(R.string.history_cleared), Toast.LENGTH_SHORT).show();
    }

    private String[] buildHistoryItems(String history) {
        if (history == null || history.trim().isEmpty()) {
            return new String[0];
        }
        String[] entries = history.split("\n");
        ArrayList<String> items = new ArrayList<>();
        for (String entry : entries) {
            String[] parts = entry.split(Pattern.quote(HISTORY_DELIMITER));
            if (parts.length < 3) {
                continue;
            }
            int boilerKw;
            try {
                boilerKw = Integer.parseInt(parts[2]);
            } catch (NumberFormatException exception) {
                continue;
            }
            items.add(getString(R.string.history_entry, parts[0], parts[1], boilerKw));
        }
        return items.toArray(new String[0]);
    }

    private void showLanguageDialog() {
        String[] languages = getResources().getStringArray(R.array.language_options_array);
        String currentLanguage = preferences.getString(KEY_LANGUAGE, LANGUAGE_CODES[0]);
        int selectedIndex = Arrays.asList(LANGUAGE_CODES).indexOf(currentLanguage);
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language_title)
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String code = LANGUAGE_CODES[which];
                    if (!code.equals(currentLanguage)) {
                        preferences.edit().putString(KEY_LANGUAGE, code).apply();
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

    private String resolveEntry(int arrayResId, int position, String fallback) {
        String[] entries = getResources().getStringArray(arrayResId);
        if (position >= 0 && position < entries.length) {
            return entries[position];
        }
        return fallback;
    }

    private boolean isUnderfloorOnly() {
        return systemPosition == 1;
    }

    private File createPdfReport(String reportText) throws IOException {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(PDF_TEXT_SIZE);
        paint.setAntiAlias(true);

        int x = PDF_MARGIN_X;
        int y = PDF_START_Y;
        for (String line : reportText.split("\n")) {
            canvas.drawText(line, x, y, paint);
            y += PDF_LINE_HEIGHT;
        }
        document.finishPage(page);

        File file = new File(getCacheDir(), "heating_report.pdf");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            document.writeTo(outputStream);
        } finally {
            document.close();
        }
        return file;
    }

    private Double parseDouble(TextInputEditText editText) {
        String value = valueOrEmpty(editText);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String valueOrEmpty(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private interface SelectionListener {
        void onSelected(int position);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
