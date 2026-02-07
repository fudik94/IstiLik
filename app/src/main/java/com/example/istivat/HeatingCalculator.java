package com.example.istivat;

import androidx.annotation.NonNull;

public final class HeatingCalculator {

    private static final double BASE_LOAD_APARTMENT_W_M2 = 100.0;
    private static final double BASE_LOAD_HOUSE_W_M2 = 120.0;
    private static final double HEIGHT_REFERENCE_M = 2.7;
    private static final double FLOOR_HEATING_W_M2 = 75.0;
    private static final double MIN_LOAD_FACTOR = 0.4;
    private static final double RADIATOR_SAFETY_FACTOR = 1.1;
    private static final double BOILER_SAFETY_FACTOR = 1.15;
    private static final double UNDERFLOOR_BOILER_FACTOR = 1.05;
    private static final double DHW_POINT_KW = 1.0;
    private static final double BOILER_THRESHOLD_SMALL_KW = 12.0;
    private static final double BOILER_THRESHOLD_MEDIUM_KW = 20.0;
    private static final double BOILER_THRESHOLD_LARGE_KW = 28.0;
    private static final double BOILER_THRESHOLD_XL_KW = 35.0;
    private static final int BOILER_SIZE_SMALL_KW = 24;
    private static final int BOILER_SIZE_MEDIUM_KW = 28;
    private static final int BOILER_SIZE_LARGE_KW = 32;
    private static final int BOILER_SIZE_XL_KW = 40;
    private static final int BOILER_SIZE_XXL_KW = 50;

    private HeatingCalculator() {
    }

    public enum HousingType {
        APARTMENT,
        HOUSE
    }

    public enum InsulationLevel {
        GOOD,
        MEDIUM,
        POOR
    }

    public enum FloorType {
        STANDARD,
        ATTIC
    }

    public enum SystemType {
        MIXED,
        UNDERFLOOR_ONLY
    }

    public static final class Input {
        public final double area;
        public final double height;
        public final int wallCount;
        public final double radiatorPower;
        public final double floorArea;
        public final HousingType housingType;
        public final InsulationLevel insulationLevel;
        public final FloorType floorType;
        public final SystemType systemType;
        public final int dhwPoints;

        public Input(double area,
                     double height,
                     int wallCount,
                     double radiatorPower,
                     double floorArea,
                     @NonNull HousingType housingType,
                     @NonNull InsulationLevel insulationLevel,
                     @NonNull FloorType floorType,
                     @NonNull SystemType systemType,
                     int dhwPoints) {
            this.area = area;
            this.height = height;
            this.wallCount = wallCount;
            this.radiatorPower = radiatorPower;
            this.floorArea = floorArea;
            this.housingType = housingType;
            this.insulationLevel = insulationLevel;
            this.floorType = floorType;
            this.systemType = systemType;
            this.dhwPoints = dhwPoints;
        }
    }

    public static final class Result {
        public final double requiredPowerKw;
        public final int radiatorSections;
        public final int recommendedBoilerKw;

        public Result(double requiredPowerKw, int radiatorSections, int recommendedBoilerKw) {
            this.requiredPowerKw = requiredPowerKw;
            this.radiatorSections = radiatorSections;
            this.recommendedBoilerKw = recommendedBoilerKw;
        }
    }

    @NonNull
    public static Result calculate(@NonNull Input input) {
        double baseLoad = input.housingType == HousingType.HOUSE
                ? BASE_LOAD_HOUSE_W_M2
                : BASE_LOAD_APARTMENT_W_M2;

        double heightFactor = input.height / HEIGHT_REFERENCE_M;
        double insulationFactor = getInsulationFactor(input.insulationLevel);
        double wallFactor = 1.0 + 0.1 * Math.max(0, input.wallCount - 1);
        double floorFactor = input.floorType == FloorType.ATTIC ? 1.1 : 1.0;

        double heatLossWatts = input.area * baseLoad * heightFactor * insulationFactor * wallFactor * floorFactor;
        double floorHeatingOffset = Math.max(0.0, input.floorArea) * FLOOR_HEATING_W_M2;
        double adjustedWatts = Math.max(heatLossWatts - floorHeatingOffset, baseLoad * input.area * MIN_LOAD_FACTOR);

        int radiatorSections = input.systemType == SystemType.UNDERFLOOR_ONLY
                ? 0
                : (int) Math.ceil(adjustedWatts * RADIATOR_SAFETY_FACTOR / input.radiatorPower);
        double requiredPowerKw = adjustedWatts / 1000.0;
        double boilerPowerKw = requiredPowerKw * BOILER_SAFETY_FACTOR;
        if (input.systemType == SystemType.UNDERFLOOR_ONLY) {
            boilerPowerKw *= UNDERFLOOR_BOILER_FACTOR;
        }
        boilerPowerKw += input.dhwPoints * DHW_POINT_KW;
        int recommendedBoilerKw = selectBoilerSize(boilerPowerKw);

        return new Result(requiredPowerKw, radiatorSections, recommendedBoilerKw);
    }

    private static double getInsulationFactor(InsulationLevel insulationLevel) {
        switch (insulationLevel) {
            case GOOD:
                return 0.85;
            case POOR:
                return 1.2;
            case MEDIUM:
            default:
                return 1.0;
        }
    }

    private static int selectBoilerSize(double recommendedPowerKw) {
        if (recommendedPowerKw <= BOILER_THRESHOLD_SMALL_KW) {
            return BOILER_SIZE_SMALL_KW;
        }
        if (recommendedPowerKw <= BOILER_THRESHOLD_MEDIUM_KW) {
            return BOILER_SIZE_MEDIUM_KW;
        }
        if (recommendedPowerKw <= BOILER_THRESHOLD_LARGE_KW) {
            return BOILER_SIZE_LARGE_KW;
        }
        if (recommendedPowerKw <= BOILER_THRESHOLD_XL_KW) {
            return BOILER_SIZE_XL_KW;
        }
        return BOILER_SIZE_XXL_KW;
    }
}
