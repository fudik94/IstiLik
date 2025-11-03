package com.example.istivat;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText editArea, editHeight, editRadiatorPower, editFloorArea;
    private Spinner spinnerHousing, spinnerWalls, spinnerInsulation, spinnerFloor;
    private Button btnCalculate;
    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Инициализация UI ---
        editArea = findViewById(R.id.editArea);
        editHeight = findViewById(R.id.editHeight);
        editRadiatorPower = findViewById(R.id.editRadiatorPower);
        editFloorArea = findViewById(R.id.editFloorArea);

        spinnerHousing = findViewById(R.id.spinnerHousing);
        spinnerWalls = findViewById(R.id.spinnerWalls);
        spinnerInsulation = findViewById(R.id.spinnerInsulation);
        spinnerFloor = findViewById(R.id.spinnerFloor);

        btnCalculate = findViewById(R.id.btnCalculate);
        textResult = findViewById(R.id.textResult);

        // --- Настройка спиннеров с кастомным layout ---
        setupSpinner(spinnerHousing, R.array.housing_array);
        setupSpinner(spinnerWalls, R.array.walls_array);
        setupSpinner(spinnerInsulation, R.array.insulation_array);
        setupSpinner(spinnerFloor, R.array.floor_array);

        // --- Кнопка расчета ---
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateHeating();
            }
        });
    }

    // --- Метод для настройки Spinner с кастомным layout ---
    private void setupSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item, // кастомный layout для выбранного элемента
                getResources().getStringArray(arrayResId)
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item); // layout для выпадающего списка
        spinner.setAdapter(adapter);
    }

    private void calculateHeating() {
        if (editArea.getText().toString().isEmpty() ||
                editHeight.getText().toString().isEmpty() ||
                editRadiatorPower.getText().toString().isEmpty() ||
                editFloorArea.getText().toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill), Toast.LENGTH_SHORT).show();
            return;
        }

        double area = Double.parseDouble(editArea.getText().toString());
        double height = Double.parseDouble(editHeight.getText().toString());
        double radiatorPower = Double.parseDouble(editRadiatorPower.getText().toString());
        double floorArea = Double.parseDouble(editFloorArea.getText().toString());

        String housingType = spinnerHousing.getSelectedItem().toString();
        int walls = Integer.parseInt(spinnerWalls.getSelectedItem().toString());
        String insulation = spinnerInsulation.getSelectedItem().toString();
        String floorType = spinnerFloor.getSelectedItem().toString();

        // --- Формулы с сохранением логики ---
        double sBase = housingType.equals("Həyət evi") ? area / 1.2 : area / 1.5;
        double sHeight = sBase * (height / 3.0);

        double sIns = sHeight;
        switch (insulation) {
            case "Yaxşı": sIns *= 0.7; break;
            case "Orta": sIns *= 1.0; break;
            case "Pis": sIns *= 1.3; break;
        }

        double sWalls = sIns * (1 + 0.1 * (walls - 1));
        double sFloor = floorType.equals("Mansard") ? sWalls * 1.1 : sWalls;
        double sFinal = sFloor - (floorArea * 100 / radiatorPower);
        if (sFinal < 1) sFinal = 1;

        // Количество секций (округляем вверх)
        int sections = (int) Math.ceil(sFinal);

        // Мощность отопления (кВт)
        double power = (sections * radiatorPower) / 1000.0;
        int powerRounded = (int) Math.ceil(power);

        // Подбор котла
        int recommendedBoiler;
        if (powerRounded <= 12) recommendedBoiler = 24;
        else if (powerRounded <= 120) recommendedBoiler = 28;
        else if (powerRounded <= 170) recommendedBoiler = 32;
        else if (powerRounded <= 240) recommendedBoiler = 40;
        else recommendedBoiler = 50;

        // Вывод результата
        String resultText = getString(R.string.result_power, powerRounded) + "\n" +
                getString(R.string.result_sections, sections) + "\n" +
                getString(R.string.result_boiler, recommendedBoiler);

        textResult.setText(resultText);
    }
}
