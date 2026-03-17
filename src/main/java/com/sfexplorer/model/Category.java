package com.sfexplorer.model;

public enum Category {
    EAT_AND_DRINK("Eat & Drink"),
    ATTRACTIONS("Attractions"),
    EVENTS("Events"),
    OUTDOOR("Outdoor Activities");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
