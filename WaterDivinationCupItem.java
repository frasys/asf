package com.example.nen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Divinação pela água: a folha reage e revela seu tipo de Nen. */
public class WaterDivinationCupItem extends Item {
    public WaterDivinationCupItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.success(stack, true);

        ServerPlayerEntity p = (ServerPlayerEntity) user;
        NenType type = NenType.of(p);
        if (type != null) {
            p.sendMessage(Text.literal("A folha já reagiu antes. Seu Nen é de ").append(
                    Text.literal(type.display).formatted(type.color, Formatting.BOLD)), true);
            return TypedActionResult.success(stack, false);
        }

        NenType[] all = NenType.values();
        type = all[p.getRandom().nextInt(all.length)];
        p.addCommandTag(type.tag());
        p.getServerWorld().spawnParticles(ParticleTypes.ENCHANT,
                p.getX(), p.getY() + 1.2, p.getZ(), 60, 0.6, 0.8, 0.6, 0.5);
        p.sendMessage(Text.literal("A folha reagiu! Você é do tipo ").append(
                Text.literal(type.display).formatted(type.color, Formatting.BOLD)), false);
        return TypedActionResult.success(stack, false);
    }
}
