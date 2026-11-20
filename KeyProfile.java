package com.clearview;

/** The four recorded keyboards. folder = where the samples live inside the mod. */
public enum KeyProfile {
    AULA_F75("Aula F75", "aula_f75"),
    FB67("FB67", "fb67"),
    HE60("60he v2", "he60v2"),
    FROST65("Frosted 65", "frost65");

    public final String label;
    public final String folder;

    KeyProfile(String label, String folder) {
        this.label = label;
        this.folder = folder;
    }

    public KeyProfile next() {
        KeyProfile[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
