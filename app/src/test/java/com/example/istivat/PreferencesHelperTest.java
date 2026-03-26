package com.example.istivat;

import org.junit.Test;
import static org.junit.Assert.*;

public class PreferencesHelperTest {

    @Test
    public void formState_defaultValues() {
        PreferencesHelper.FormState state = PreferencesHelper.FormState.defaults();
        assertEquals("", state.area);
        assertEquals("", state.height);
        assertEquals("180", state.radiatorPower);
        assertEquals("", state.floorArea);
        assertEquals(0, state.housingPosition);
        assertEquals(0, state.wallsPosition);
        assertEquals(0, state.insulationPosition);
        assertEquals(0, state.floorPosition);
        assertEquals(0, state.systemPosition);
        assertEquals(0, state.dhwPosition);
        assertFalse(state.advancedExpanded);
        assertNull(state.resultText);
    }

    @Test
    public void formState_constructor_storesAllFields() {
        PreferencesHelper.FormState state = new PreferencesHelper.FormState(
                "50", "2.7", "180", "10",
                1, 2, 1, 0, 2, 3,
                true, "Result text"
        );
        assertEquals("50", state.area);
        assertEquals("2.7", state.height);
        assertEquals("180", state.radiatorPower);
        assertEquals("10", state.floorArea);
        assertEquals(1, state.housingPosition);
        assertEquals(2, state.wallsPosition);
        assertEquals(1, state.insulationPosition);
        assertEquals(0, state.floorPosition);
        assertEquals(2, state.systemPosition);
        assertEquals(3, state.dhwPosition);
        assertTrue(state.advancedExpanded);
        assertEquals("Result text", state.resultText);
    }
}
