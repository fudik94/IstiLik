package com.example.istivat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HeatingCalculatorTest {

    @Test
    public void calculatesHeatingForApartment() {
        HeatingCalculator.Input input = new HeatingCalculator.Input(
                50.0,
                2.8,
                2,
                180.0,
                10.0,
                HeatingCalculator.HousingType.APARTMENT,
                HeatingCalculator.InsulationLevel.MEDIUM,
                HeatingCalculator.FloorType.STANDARD,
                HeatingCalculator.SystemType.MIXED,
                1
        );

        HeatingCalculator.Result result = HeatingCalculator.calculate(input);

        assertEquals(4.95, result.requiredPowerKw, 0.05);
        assertEquals(31, result.radiatorSections);
        assertEquals(24, result.recommendedBoilerKw);
    }
}
