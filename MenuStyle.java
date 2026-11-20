package com.clearview;

public enum MenuStyle {
    OFF("Vanilla"), BLOSSOM("Blossom"), AURORA("Aurora"), MINIMAL("Minimal"), CLASSIC("Classic");

    public final String label;

    MenuStyle(String label) {
        this.label = label;
    }

    public MenuStyle next() {
        MenuStyle[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
