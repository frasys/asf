package com.example.nen;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NenMod implements ModInitializer {
    public static final String MOD_ID = "nen";

    /** Modo ativo de cada jogador (não persiste entre reinícios do servidor). */
    public static final Map<UUID, NenMode> MODES = new HashMap<>();

    public static Item HUNTER_LICENSE;
    public static Item WATER_CUP;

    @Override
    public void onInitialize() {
        HUNTER_LICENSE = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "hunter_license"),
                new HunterLicenseItem(new Item.Settings().maxCount(1)));
        WATER_CUP = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "water_divination_cup"),
                new WaterDivinationCupItem(new Item.Settings().maxCount(1)));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(HUNTER_LICENSE);
            entries.add(WATER_CUP);
        });

        // Ao morrer, mantém o tipo de Nen.
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            for (String tag : oldPlayer.getCommandTags()) {
                if (tag.startsWith("nen_type_")) newPlayer.addCommandTag(tag);
            }
            MODES.remove(oldPlayer.getUuid());
        });

        // Efeitos contínuos de Ten / Ren / Zetsu (1x por segundo).
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return;
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                NenMode m = MODES.getOrDefault(p.getUuid(), NenMode.NONE);
                switch (m) {
                    case TEN -> p.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.RESISTANCE, 40, 0, true, false, true));
                    case REN -> {
                        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 1, true, false, true));
                        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0, true, false, true));
                        p.addExhaustion(1.0f);
                        p.getServerWorld().spawnParticles(ParticleTypes.CLOUD,
                                p.getX(), p.getY() + 1.0, p.getZ(), 12, 0.4, 0.8, 0.4, 0.05);
                    }
                    case ZETSU -> p.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.INVISIBILITY, 40, 0, true, false, false));
                    default -> { }
                }
            }
        });
    }
}
