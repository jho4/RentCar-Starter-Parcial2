package edu.rentcar.extras;

/** Optional extras that the user can select for a rental. */
public enum ExtraOption {
    GPS("GPS (5/day)"),
    CHILD_SEAT("Child seat (7/day)"),
    EXTRA_DRIVER("Extra driver (30 fixed)"),
    ROAD_TRIP_BUNDLE("Road Trip Bundle (10% off)");

    private final String label;

    ExtraOption(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}