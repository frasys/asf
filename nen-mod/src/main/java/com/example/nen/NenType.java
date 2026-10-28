package com.example.nen;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public enum NenType {
    ENHANCER("Intensificação", Formatting.RED),
    EMITTER("Emissão", Formatting.GOLD),
    TRANSMUTER("Transformação", Formatting.AQUA),
    CONJURER("Materialização", Formatting.GREEN),
    MANIPULATOR("Manipulação", Formatting.LIGHT_PURPLE),
    SPECIALIST("Especialização", Formatting.WHITE);

    public final String display;
    public final Formatting color;

    NenType(String display, Formatting color) {
        this.display = display;
        this.color = color;
    }

    /** O tipo fica salvo como command tag do jogador (persiste no save). */
    public String tag() {
        return "nen_type_" + name().toLowerCase();
    }

    public static NenType of(ServerPlayerEntity p) {
        for (NenType t : values()) {
            if (p.getCommandTags().contains(t.tag())) return t;
        }
        return null;
    }
}
