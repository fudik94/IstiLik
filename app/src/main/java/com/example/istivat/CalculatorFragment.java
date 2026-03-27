package com.example.istivat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class CalculatorFragment extends Fragment {

    private static final String TAG = "CalculatorFragment";
    private static final double MIN_AREA = 5.0, MAX_AREA = 1000.0;
    private static final double MIN_HEIGHT = 2.0, MAX_HEIGHT = 6.0;
    private static final double MIN_RADIATOR_POWER = 50.0, MAX_RADIATOR_POWER = 500.0;
    private static final double DEFAULT_RADIATOR_POWER = 180.0;
    private static final int MAX_DHW_POINTS = 9;
    private static final int PDF_PAGE_WIDTH = 595, PDF_PAGE_HEIGHT = 842;
    private static final int PDF_MARGIN_X = 40, PDF_START_Y = 60, PDF_LINE_HEIGHT = 22;
    private static final float PDF_TEXT_SIZE = 14f;

    // Views
    private TextInputLayout layoutArea, layoutHeight, layoutRadiatorPower, layoutFloorArea;
    private TextInputEditText editArea, editHeight, editRadiatorPower, editFloorArea;
    private MaterialAutoCompleteTextView autoWalls, autoInsulation, autoFloor, autoDhwPoints;
    private MaterialButtonToggleGroup toggleHousing, toggleSystem;
    private MaterialButton btnSaveHistory, btnReset, btnShare, btnAdvancedToggle;
    private LinearLayout advancedSection;
    private TextView textResultPower, textResultSections, textResultBoiler;
    private LinearLayout layoutResultSections;

    // State
    private int wallsPosition = 0, insulationPosition = 0, floorPosition = 0, dhwPosition = 0;
    private HeatingCalculator.Result lastResult;
    private HeatingCalculator.Input lastInput;
    private PreferencesHelper prefsHelper;
    private HistoryManager historyManager;
    private boolean isRestoring = false;
    private boolean advancedExpanded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PreferencesHelper.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefsHelper = new PreferencesHelper(prefs);
        historyManager = new HistoryManager(prefs);

        bindViews(view);
        setupDropdowns();
        setupTextWatchers();
        setupToggles();
        setupButtons();
        restoreState();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveState();
    }

    // ── View binding ────────────────────────────────────────────────────────

    private void bindViews(View view) {
        layoutArea = view.findViewById(R.id.layoutArea);
        layoutHeight = view.findViewById(R.id.layoutHeight);
        layoutRadiatorPower = view.findViewById(R.id.layoutRadiatorPower);
        layoutFloorArea = view.findViewById(R.id.layoutFloorArea);

        editArea = view.findViewById(R.id.editArea);
        editHeight = view.findViewById(R.id.editHeight);
        editRadiatorPower = view.findViewById(R.id.editRadiatorPower);
        editFloorArea = view.findViewById(R.id.editFloorArea);

        autoWalls = view.findViewById(R.id.autoWalls);
        autoInsulation = view.findViewById(R.id.autoInsulation);
        autoFloor = view.findViewById(R.id.autoFloor);
        autoDhwPoints = view.findViewById(R.id.autoDhwPoints);

        toggleHousing = view.findViewById(R.id.toggleHousing);
        toggleSystem = view.findViewById(R.id.toggleSystem);

        btnSaveHistory = view.findViewById(R.id.btnSaveHistory);
        btnReset = view.findViewById(R.id.btnReset);
        btnShare = view.findViewById(R.id.btnShare);
        btnAdvancedToggle = view.findViewById(R.id.btnAdvancedToggle);
        advancedSection = view.findViewById(R.id.advancedSection);

        textResultPower = view.findViewById(R.id.textResultPower);
        textResultSections = view.findViewById(R.id.textResultSections);
        textResultBoiler = view.findViewById(R.id.textResultBoiler);
        layoutResultSections = view.findViewById(R.id.layoutResultSections);
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private void setupDropdowns() {
        setupDropdown(autoWalls, R.array.walls_array, pos -> wallsPosition = pos);
        setupDropdown(autoInsulation, R.array.insulation_array, pos -> insulationPosition = pos);
        setupDropdown(autoFloor, R.array.floor_array, pos -> floorPosition = pos);
        setupDropdown(autoDhwPoints, R.array.dhw_points_array, pos -> dhwPosition = pos);
    }

    private void setupDropdown(MaterialAutoCompleteTextView view, int arrayResId,
                                PositionListener listener) {
        final String[] items = getResources().getStringArray(arrayResId);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                R.layout.spinner_item, items) {
            @Override
            public android.widget.Filter getFilter() {
                return new android.widget.Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence c) {
                        FilterResults r = new FilterResults();
                        r.values = items;
                        r.count = items.length;
                        return r;
                    }
                    @Override
                    protected void publishResults(CharSequence c, FilterResults r) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        view.setAdapter(adapter);
        view.setOnItemClickListener((parent, v, position, id) -> {
            listener.onPosition(position);
            recalculate();
        });
        listener.onPosition(0);
        view.setText(adapter.getItem(0), false);
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!isRestoring) recalculate();
            }
        };
        editArea.addTextChangedListener(watcher);
        editHeight.addTextChangedListener(watcher);
        editRadiatorPower.addTextChangedListener(watcher);
        editFloorArea.addTextChangedListener(watcher);
    }

    private void setupToggles() {
        toggleHousing.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && !isRestoring) recalculate();
        });
        toggleSystem.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && !isRestoring) {
                updateSystemMode();
                recalculate();
            }
        });
        toggleHousing.check(R.id.btnApartment);
        toggleSystem.check(R.id.btnRadiators);
    }

    private void setupButtons() {
        btnAdvancedToggle.setOnClickListener(v -> toggleAdvancedSection());
        btnSaveHistory.setOnClickListener(v -> saveToHistory());
        btnReset.setOnClickListener(v -> resetForm());
        btnShare.setOnClickListener(v -> shareReport());
    }

    // ── Advanced section ─────────────────────────────────────────────────────

    private void toggleAdvancedSection() {
        advancedExpanded = !advancedExpanded;
        TransitionManager.beginDelayedTransition((ViewGroup) requireView(), new AutoTransition());
        advancedSection.setVisibility(advancedExpanded ? View.VISIBLE : View.GONE);
        btnAdvancedToggle.setText(advancedExpanded
                ? R.string.advanced_params_hide
                : R.string.advanced_params_show);
    }

    private void setAdvancedExpanded(boolean expanded) {
        advancedExpanded = expanded;
        advancedSection.setVisibility(expanded ? View.VISIBLE : View.GONE);
        btnAdvancedToggle.setText(expanded
                ? R.string.advanced_params_hide
                : R.string.advanced_params_show);
    }

    // ── System mode ──────────────────────────────────────────────────────────

    private void updateSystemMode() {
        int checkedId = toggleSystem.getCheckedButtonId();
        boolean underfloorOnly = checkedId == R.id.btnUnderfloor;
        boolean radiatorsOnly = checkedId == R.id.btnRadiators;

        layoutRadiatorPower.setVisibility(underfloorOnly ? View.GONE : View.VISIBLE);
        layoutFloorArea.setVisibility(radiatorsOnly ? View.GONE : View.VISIBLE);
        layoutResultSections.setVisibility(underfloorOnly ? View.GONE : View.VISIBLE);

        if (underfloorOnly) layoutRadiatorPower.setError(null);
    }

    // ── Calculation ──────────────────────────────────────────────────────────

    private void recalculate() {
        if (!validateForm()) {
            showDashResults();
            return;
        }
        HeatingCalculator.Input input = buildInput();
        if (input == null) {
            showDashResults();
            return;
        }
        lastInput = input;
        lastResult = HeatingCalculator.calculate(input);
        showResults(lastResult, input);
        btnSaveHistory.setEnabled(true);
        btnShare.setEnabled(true);
    }

    private void showResults(HeatingCalculator.Result result, HeatingCalculator.Input input) {
        textResultPower.setText(String.format(Locale.getDefault(), "%.1f", result.requiredPowerKw));
        textResultSections.setText(String.valueOf(result.radiatorSections));
        textResultBoiler.setText(String.valueOf(result.recommendedBoilerKw));
        prefsHelper.saveResultText(buildResultText(input, result));
    }

    private void showDashResults() {
        String dash = getString(R.string.result_dash);
        textResultPower.setText(dash);
        textResultSections.setText(dash);
        textResultBoiler.setText(dash);
        lastResult = null;
        lastInput = null;
        btnSaveHistory.setEnabled(false);
        btnShare.setEnabled(false);
        prefsHelper.saveResultText(null);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private boolean validateForm() {
        boolean valid = true;
        boolean underfloorOnly = toggleSystem.getCheckedButtonId() == R.id.btnUnderfloor;
        boolean radiatorsOnly = toggleSystem.getCheckedButtonId() == R.id.btnRadiators;

        Double area = parseDouble(editArea);
        if (area == null || area < MIN_AREA || area > MAX_AREA) {
            if (!isRestoring && area != null) layoutArea.setError(getString(R.string.error_invalid_area));
            valid = false;
        } else {
            layoutArea.setError(null);
        }

        Double height = parseDouble(editHeight);
        if (height == null || height < MIN_HEIGHT || height > MAX_HEIGHT) {
            if (!isRestoring && height != null) layoutHeight.setError(getString(R.string.error_invalid_height));
            valid = false;
        } else {
            layoutHeight.setError(null);
        }

        if (!underfloorOnly) {
            Double rp = parseDouble(editRadiatorPower);
            if (rp == null || rp < MIN_RADIATOR_POWER || rp > MAX_RADIATOR_POWER) {
                if (!isRestoring && rp != null) layoutRadiatorPower.setError(getString(R.string.error_invalid_radiator));
                valid = false;
            } else {
                layoutRadiatorPower.setError(null);
            }
        }

        if (!radiatorsOnly) {
            Double floorArea = parseDouble(editFloorArea);
            double maxFloor = area != null ? area : MAX_AREA;
            if (floorArea == null || floorArea < 0 || (area != null && floorArea > area)) {
                if (!isRestoring && floorArea != null)
                    layoutFloorArea.setError(getString(R.string.error_invalid_floor_area, maxFloor));
                valid = false;
            } else {
                layoutFloorArea.setError(null);
            }
        }

        return valid;
    }

    // ── Input building ───────────────────────────────────────────────────────

    private HeatingCalculator.Input buildInput() {
        Double area = parseDouble(editArea);
        Double height = parseDouble(editHeight);
        if (area == null || height == null) return null;

        int checkedSystemId = toggleSystem.getCheckedButtonId();
        boolean underfloorOnly = checkedSystemId == R.id.btnUnderfloor;
        boolean radiatorsOnly = checkedSystemId == R.id.btnRadiators;

        double radiatorPower = DEFAULT_RADIATOR_POWER;
        if (!underfloorOnly) {
            Double rp = parseDouble(editRadiatorPower);
            if (rp == null) return null;
            radiatorPower = rp;
        }

        double floorArea = 0.0;
        if (!radiatorsOnly) {
            Double fa = parseDouble(editFloorArea);
            if (fa == null) return null;
            floorArea = fa;
        }

        boolean isHouse = toggleHousing.getCheckedButtonId() == R.id.btnHouse;
        int wallCount = wallsPosition + 1;
        int dhwPoints = Math.min(Math.max(dhwPosition, 0), MAX_DHW_POINTS - 1) + 1;

        return new HeatingCalculator.Input(
                area, height, wallCount, radiatorPower, floorArea,
                isHouse ? HeatingCalculator.HousingType.HOUSE : HeatingCalculator.HousingType.APARTMENT,
                mapInsulation(insulationPosition),
                floorPosition == 1 ? HeatingCalculator.FloorType.ATTIC : HeatingCalculator.FloorType.STANDARD,
                underfloorOnly ? HeatingCalculator.SystemType.UNDERFLOOR_ONLY : HeatingCalculator.SystemType.MIXED,
                dhwPoints
        );
    }

    private HeatingCalculator.InsulationLevel mapInsulation(int position) {
        if (position == 0) return HeatingCalculator.InsulationLevel.GOOD;
        if (position == 2) return HeatingCalculator.InsulationLevel.POOR;
        return HeatingCalculator.InsulationLevel.MEDIUM;
    }

    // ── History ──────────────────────────────────────────────────────────────

    private void saveToHistory() {
        if (lastInput == null || lastResult == null) return;

        android.widget.EditText nameInput = new android.widget.EditText(requireContext());
        nameInput.setHint(getString(R.string.history_save_name_hint));
        nameInput.setSingleLine(true);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        nameInput.setPadding(padding, padding, padding, padding);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.history_save_name_title)
                .setView(nameInput)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    historyManager.addEntry(name, lastInput, lastResult);
                    Toast.makeText(requireContext(), getString(R.string.save_to_history), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // ── Reset ────────────────────────────────────────────────────────────────

    private void resetForm() {
        isRestoring = true;
        editArea.setText("");
        editHeight.setText("");
        editRadiatorPower.setText(String.valueOf((int) DEFAULT_RADIATOR_POWER));
        editFloorArea.setText("");
        toggleHousing.check(R.id.btnApartment);
        toggleSystem.check(R.id.btnRadiators);
        setDropdownPosition(autoWalls, R.array.walls_array, 0, pos -> wallsPosition = pos);
        setDropdownPosition(autoInsulation, R.array.insulation_array, 0, pos -> insulationPosition = pos);
        setDropdownPosition(autoFloor, R.array.floor_array, 0, pos -> floorPosition = pos);
        setDropdownPosition(autoDhwPoints, R.array.dhw_points_array, 0, pos -> dhwPosition = pos);
        layoutArea.setError(null);
        layoutHeight.setError(null);
        layoutRadiatorPower.setError(null);
        layoutFloorArea.setError(null);
        showDashResults();
        updateSystemMode();
        saveState();
        isRestoring = false;
    }

    // ── State save/restore ───────────────────────────────────────────────────

    private void saveState() {
        int housingPos = toggleHousing.getCheckedButtonId() == R.id.btnHouse ? 1 : 0;
        int systemPos = systemToggleToPosition();
        prefsHelper.save(new PreferencesHelper.FormState(
                valueOrEmpty(editArea), valueOrEmpty(editHeight),
                valueOrEmpty(editRadiatorPower), valueOrEmpty(editFloorArea),
                housingPos, wallsPosition, insulationPosition, floorPosition,
                systemPos, dhwPosition, advancedExpanded, null
        ));
    }

    private void restoreState() {
        isRestoring = true;
        PreferencesHelper.FormState state = prefsHelper.load();

        editArea.setText(state.area);
        editHeight.setText(state.height);
        editRadiatorPower.setText(state.radiatorPower);
        editFloorArea.setText(state.floorArea);

        toggleHousing.check(state.housingPosition == 1 ? R.id.btnHouse : R.id.btnApartment);
        toggleSystem.check(systemPositionToButtonId(state.systemPosition));

        setDropdownPosition(autoWalls, R.array.walls_array, state.wallsPosition, pos -> wallsPosition = pos);
        setDropdownPosition(autoInsulation, R.array.insulation_array, state.insulationPosition, pos -> insulationPosition = pos);
        setDropdownPosition(autoFloor, R.array.floor_array, state.floorPosition, pos -> floorPosition = pos);
        setDropdownPosition(autoDhwPoints, R.array.dhw_points_array, state.dhwPosition, pos -> dhwPosition = pos);

        setAdvancedExpanded(state.advancedExpanded);
        updateSystemMode();
        isRestoring = false;
        recalculate();
    }

    private int systemToggleToPosition() {
        int id = toggleSystem.getCheckedButtonId();
        if (id == R.id.btnUnderfloor) return 1;
        if (id == R.id.btnMixed) return 2;
        return 0;
    }

    private int systemPositionToButtonId(int pos) {
        if (pos == 1) return R.id.btnUnderfloor;
        if (pos == 2) return R.id.btnMixed;
        return R.id.btnRadiators;
    }

    // ── Share / PDF ──────────────────────────────────────────────────────────

    private void shareReport() {
        if (lastInput == null || lastResult == null) return;
        try {
            File pdfFile = createPdfReport(lastInput, lastResult);
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_share_message));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getString(R.string.report_share_title)));
        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, "Failed to share PDF", e);
            Toast.makeText(requireContext(), getString(R.string.report_share_error), Toast.LENGTH_SHORT).show();
        }
    }

    private String buildResultText(HeatingCalculator.Input input, HeatingCalculator.Result result) {
        boolean underfloorOnly = input.systemType == HeatingCalculator.SystemType.UNDERFLOOR_ONLY;
        StringBuilder sb = new StringBuilder(getString(R.string.result_power, result.requiredPowerKw));
        if (!underfloorOnly) sb.append("\n").append(getString(R.string.result_sections, result.radiatorSections));
        sb.append("\n").append(getString(R.string.result_boiler, result.recommendedBoilerKw));
        return sb.toString();
    }

    private String buildReportText(HeatingCalculator.Input input, HeatingCalculator.Result result) {
        boolean underfloorOnly = input.systemType == HeatingCalculator.SystemType.UNDERFLOOR_ONLY;
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.report_title)).append("\n\n");
        sb.append(getString(R.string.area)).append(": ").append(String.format(Locale.getDefault(), "%.1f m²\n", input.area));
        sb.append(getString(R.string.height)).append(": ").append(String.format(Locale.getDefault(), "%.1f m\n", input.height));
        if (!underfloorOnly) sb.append(getString(R.string.radiator_power)).append(": ").append(String.format(Locale.getDefault(), "%.0f W\n", input.radiatorPower));
        sb.append(getString(R.string.floor_area)).append(": ").append(String.format(Locale.getDefault(), "%.1f m²\n\n", input.floorArea));
        sb.append(getString(R.string.result_power, result.requiredPowerKw)).append("\n");
        if (!underfloorOnly) sb.append(getString(R.string.result_sections, result.radiatorSections)).append("\n");
        sb.append(getString(R.string.result_boiler, result.recommendedBoilerKw));
        return sb.toString();
    }

    private File createPdfReport(HeatingCalculator.Input input, HeatingCalculator.Result result) throws IOException {
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = doc.startPage(info);
        Canvas canvas = page.getCanvas();

        boolean underfloorOnly = input.systemType == HeatingCalculator.SystemType.UNDERFLOOR_ONLY;

        // ── Header background ────────────────────────────────────────────────
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(android.graphics.Color.parseColor("#1A1A24"));
        canvas.drawRect(0, 0, PDF_PAGE_WIDTH, 110, bgPaint);

        // Flame icon circle
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(android.graphics.Color.parseColor("#FF6B35"));
        canvas.drawCircle(52, 55, 26, circlePaint);

        Paint iconTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconTextPaint.setColor(android.graphics.Color.WHITE);
        iconTextPaint.setTextSize(22f);
        iconTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("\u2668", 52, 63, iconTextPaint);

        // App name
        Paint appNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        appNamePaint.setColor(android.graphics.Color.WHITE);
        appNamePaint.setTextSize(26f);
        appNamePaint.setFakeBoldText(true);
        canvas.drawText("IstiLik", 92, 48, appNamePaint);

        // Report subtitle
        Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitlePaint.setColor(android.graphics.Color.parseColor("#9E9EB0"));
        subtitlePaint.setTextSize(12f);
        canvas.drawText(getString(R.string.report_title), 92, 68, subtitlePaint);

        // Date
        String dateStr = java.text.DateFormat.getDateTimeInstance(
                java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(new java.util.Date());
        Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datePaint.setColor(android.graphics.Color.parseColor("#9E9EB0"));
        datePaint.setTextSize(10f);
        datePaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(dateStr, PDF_PAGE_WIDTH - 24, 68, datePaint);

        // ── Orange accent bar ────────────────────────────────────────────────
        Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(android.graphics.Color.parseColor("#FF6B35"));
        canvas.drawRect(0, 110, PDF_PAGE_WIDTH, 115, accentPaint);

        // ── Section: Parameters ──────────────────────────────────────────────
        int y = 148;
        int labelX = PDF_MARGIN_X;
        int valueX = PDF_PAGE_WIDTH / 2 + 20;

        Paint sectionLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectionLabelPaint.setColor(android.graphics.Color.parseColor("#FF6B35"));
        sectionLabelPaint.setTextSize(11f);
        sectionLabelPaint.setFakeBoldText(true);
        canvas.drawText("PARAMETERS", labelX, y, sectionLabelPaint);
        y += 6;

        Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(android.graphics.Color.parseColor("#E0E0E0"));
        canvas.drawLine(labelX, y, PDF_PAGE_WIDTH - PDF_MARGIN_X, y, dividerPaint);
        y += 18;

        Paint keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        keyPaint.setColor(android.graphics.Color.parseColor("#555555"));
        keyPaint.setTextSize(12f);

        Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(android.graphics.Color.parseColor("#111111"));
        valuePaint.setTextSize(12f);
        valuePaint.setFakeBoldText(true);

        // Parameters rows
        String[][] params = {
            {getString(R.string.area), String.format(Locale.getDefault(), "%.1f m\u00B2", input.area)},
            {getString(R.string.height), String.format(Locale.getDefault(), "%.1f m", input.height)},
            {getString(R.string.housing_type), input.housingType == HeatingCalculator.HousingType.HOUSE
                    ? getString(R.string.housing_house) : getString(R.string.housing_apartment)},
            {getString(R.string.walls), String.valueOf(input.wallCount)},
            {getString(R.string.insulation), mapInsulationName(input.insulationLevel)},
            {getString(R.string.floor_type), input.floorType == HeatingCalculator.FloorType.ATTIC
                    ? getResources().getStringArray(R.array.floor_array)[1]
                    : getResources().getStringArray(R.array.floor_array)[0]},
            {getString(R.string.dhw_points), String.valueOf(input.dhwPoints)},
        };

        if (!underfloorOnly) {
            params = appendParam(params, getString(R.string.radiator_power),
                    String.format(Locale.getDefault(), "%.0f W", input.radiatorPower));
        }
        if (input.floorArea > 0) {
            params = appendParam(params, getString(R.string.floor_area),
                    String.format(Locale.getDefault(), "%.1f m\u00B2", input.floorArea));
        }

        for (String[] row : params) {
            canvas.drawText(row[0], labelX, y, keyPaint);
            canvas.drawText(row[1], valueX, y, valuePaint);
            y += PDF_LINE_HEIGHT + 2;
        }

        y += 10;
        canvas.drawLine(labelX, y, PDF_PAGE_WIDTH - PDF_MARGIN_X, y, dividerPaint);
        y += 22;

        // ── Section: Results ─────────────────────────────────────────────────
        canvas.drawText("RESULTS", labelX, y, sectionLabelPaint);
        y += 6;
        canvas.drawLine(labelX, y, PDF_PAGE_WIDTH - PDF_MARGIN_X, y, dividerPaint);
        y += 22;

        Paint resultKeyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultKeyPaint.setColor(android.graphics.Color.parseColor("#555555"));
        resultKeyPaint.setTextSize(13f);

        Paint resultValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultValuePaint.setColor(android.graphics.Color.parseColor("#FF6B35"));
        resultValuePaint.setTextSize(18f);
        resultValuePaint.setFakeBoldText(true);

        canvas.drawText(getString(R.string.result_label_power), labelX, y, resultKeyPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.1f kW", result.requiredPowerKw),
                valueX, y, resultValuePaint);
        y += PDF_LINE_HEIGHT + 8;

        if (!underfloorOnly) {
            canvas.drawText(getString(R.string.result_label_sections), labelX, y, resultKeyPaint);
            canvas.drawText(result.radiatorSections + " pcs", valueX, y, resultValuePaint);
            y += PDF_LINE_HEIGHT + 8;
        }

        canvas.drawText(getString(R.string.result_label_boiler), labelX, y, resultKeyPaint);
        canvas.drawText(result.recommendedBoilerKw + " kW", valueX, y, resultValuePaint);

        // ── Footer ───────────────────────────────────────────────────────────
        Paint footerBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerBgPaint.setColor(android.graphics.Color.parseColor("#F5F5F5"));
        canvas.drawRect(0, PDF_PAGE_HEIGHT - 36, PDF_PAGE_WIDTH, PDF_PAGE_HEIGHT, footerBgPaint);

        Paint footerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerPaint.setColor(android.graphics.Color.parseColor("#9E9EB0"));
        footerPaint.setTextSize(10f);
        canvas.drawText("IstiLik \u2014 Heating Calculator", labelX, PDF_PAGE_HEIGHT - 16, footerPaint);
        footerPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(dateStr, PDF_PAGE_WIDTH - PDF_MARGIN_X, PDF_PAGE_HEIGHT - 16, footerPaint);

        doc.finishPage(page);
        File file = new File(requireContext().getCacheDir(), "heating_report.pdf");
        try (FileOutputStream out = new FileOutputStream(file)) {
            doc.writeTo(out);
        } finally {
            doc.close();
        }
        return file;
    }

    private String mapInsulationName(HeatingCalculator.InsulationLevel level) {
        String[] arr = getResources().getStringArray(R.array.insulation_array);
        if (level == HeatingCalculator.InsulationLevel.GOOD) return arr[0];
        if (level == HeatingCalculator.InsulationLevel.POOR) return arr[2];
        return arr[1];
    }

    private String[][] appendParam(String[][] original, String key, String value) {
        String[][] newArr = new String[original.length + 1][2];
        System.arraycopy(original, 0, newArr, 0, original.length);
        newArr[original.length] = new String[]{key, value};
        return newArr;
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private void setDropdownPosition(MaterialAutoCompleteTextView view, int arrayResId,
                                     int position, PositionListener listener) {
        String[] entries = getResources().getStringArray(arrayResId);
        int safe = Math.min(Math.max(position, 0), entries.length - 1);
        listener.onPosition(safe);
        // Prefer the adapter's item so the displayed text always matches the dropdown list locale
        android.widget.ListAdapter adapter = view.getAdapter();
        String text = (adapter != null && safe < adapter.getCount())
                ? (String) adapter.getItem(safe)
                : entries[safe];
        view.setText(text, false);
    }

    private Double parseDouble(TextInputEditText editText) {
        String val = valueOrEmpty(editText);
        if (val.isEmpty()) return null;
        try { return Double.parseDouble(val.replace(',', '.')); }
        catch (NumberFormatException e) { return null; }
    }

    private String valueOrEmpty(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private interface PositionListener {
        void onPosition(int position);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
