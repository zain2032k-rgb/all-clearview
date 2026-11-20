package com.clearview;

public enum SwingStyle {
    VANILLA("Vanilla"), SMOOTH("Smooth"), SLIDE("Slide"), STAB("Stab"),
    SPIN("Spin"), SWIRL("Swirl"), CHOP("Chop");

    public final String label;

    SwingStyle(String label) {
        this.label = label;
    }

    public SwingStyle next() {
        SwingStyle[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
