package com.example.nen;

public enum NenMode {
    NONE("Nenhum"), TEN("Ten"), REN("Ren"), ZETSU("Zetsu");

    public final String display;

    NenMode(String display) { this.display = display; }

    public NenMode next() {
        NenMode[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
