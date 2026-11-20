package com.clearview;

public enum Accent {
    BLOSSOM("Blossom", 0xFFFF8FB8), PURPLE("Purple", 0xFF8B5CF6), BLUE("Blue", 0xFF3B82F6),
    GREEN("Green", 0xFF34D399), YELLOW("Yellow", 0xFFFFD21F);

    public final String label;
    public final int color;

    Accent(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public Accent next() {
        Accent[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
