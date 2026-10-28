package com.example.nen;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Licença de Hunter.
 *  - Agachado + clique direito: alterna entre Nenhum -> Ten -> Ren -> Zetsu.
 *  - Clique direito: usa o Hatsu do seu tipo de Nen (custa fome).
 */
public class HunterLicenseItem extends Item {
    public HunterLicenseItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.success(stack, true);

        ServerPlayerEntity p = (ServerPlayerEntity) user;

        if (p.isSneaking()) {
            NenMode next = NenMod.MODES.getOrDefault(p.getUuid(), NenMode.NONE).next();
            NenMod.MODES.put(p.getUuid(), next);
            p.sendMessage(Text.literal("Modo de Nen: ").append(
                    Text.literal(next.display).formatted(Formatting.YELLOW, Formatting.BOLD)), true);
            return TypedActionResult.success(stack, false);
        }

        NenType type = NenType.of(p);
        if (type == null) {
            p.sendMessage(Text.literal("Você ainda não despertou o Nen. Use o Copo de Divinação.")
                    .formatted(Formatting.RED), true);
            return TypedActionResult.fail(stack);
        }
        if (p.getHungerManager().getFoodLevel() < 6) {
            p.sendMessage(Text.literal("Aura insuficiente (coma algo).").formatted(Formatting.RED), true);
            return TypedActionResult.fail(stack);
        }

        p.addExhaustion(6.0f);
        p.getItemCooldownManager().set(this, 40);
        hatsu(p, p.getServerWorld(), type);
        return TypedActionResult.success(stack, false);
    }

    private void hatsu(ServerPlayerEntity p, ServerWorld w, NenType type) {
        Vec3d look = p.getRotationVec(1.0f);
        switch (type) {
            case ENHANCER -> {
                LivingEntity t = target(p, 5);
                if (t != null) {
                    t.damage(p.getDamageSources().playerAttack(p), 14.0f);
                    t.takeKnockback(2.0, -look.x, -look.z);
                    w.spawnParticles(ParticleTypes.EXPLOSION, t.getX(), t.getBodyY(0.5), t.getZ(), 2, 0.2, 0.2, 0.2, 0);
                } else {
                    Vec3d f = p.getEyePos().add(look.multiply(2));
                    w.spawnParticles(ParticleTypes.SWEEP_ATTACK, f.x, f.y, f.z, 1, 0, 0, 0, 0);
                }
            }
            case EMITTER -> {
                LivingEntity t = target(p, 24);
                double dist = 24;
                if (t != null) {
                    dist = p.getEyePos().distanceTo(t.getBoundingBox().getCenter());
                    t.damage(p.getDamageSources().playerAttack(p), 8.0f);
                    t.setOnFireFor(4);
                }
                Vec3d s = p.getEyePos();
                for (double i = 1; i < dist; i += 0.5) {
                    Vec3d pt = s.add(look.multiply(i));
                    w.spawnParticles(ParticleTypes.END_ROD, pt.x, pt.y, pt.z, 1, 0, 0, 0, 0);
                }
            }
            case TRANSMUTER -> {
                LivingEntity t = target(p, 24);
                Vec3d pos;
                if (t != null) {
                    pos = t.getPos();
                } else {
                    HitResult hit = p.raycast(24, 0, false);
                    pos = hit.getPos();
                }
                if (pos.distanceTo(p.getPos()) < 3) {
                    p.sendMessage(Text.literal("Alvo muito perto!").formatted(Formatting.RED), true);
                    return;
                }
                LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
                if (bolt != null) {
                    bolt.refreshPositionAfterTeleport(pos);
                    bolt.setChanneler(p);
                    w.spawnEntity(bolt);
                }
            }
            case CONJURER -> {
                Box area = p.getBoundingBox().expand(8);
                for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, area, en -> en != p && en.isAlive())) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
                    w.spawnParticles(ParticleTypes.CRIT, e.getX(), e.getBodyY(0.5), e.getZ(), 25, 0.3, 0.6, 0.3, 0.2);
                }
            }
            case MANIPULATOR -> {
                LivingEntity t = target(p, 16);
                if (t != null) {
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1));
                    t.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
                    if (t instanceof MobEntity m) m.setTarget(null);
                    w.spawnParticles(ParticleTypes.WITCH, t.getX(), t.getBodyY(0.8), t.getZ(), 30, 0.3, 0.5, 0.3, 0.1);
                }
            }
            case SPECIALIST -> {
                p.heal(4.0f);
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 2));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 400, 2));
                w.spawnParticles(ParticleTypes.HEART, p.getX(), p.getY() + 1.5, p.getZ(), 10, 0.4, 0.4, 0.4, 0);
            }
        }
    }

    private static LivingEntity target(ServerPlayerEntity p, double range) {
        Vec3d start = p.getEyePos();
        Vec3d dir = p.getRotationVec(1.0f);
        Vec3d end = start.add(dir.multiply(range));
        Box box = p.getBoundingBox().stretch(dir.multiply(range)).expand(1.0);
        EntityHitResult r = ProjectileUtil.raycast(p, start, end, box,
                e -> !e.isSpectator() && e.canHit() && e instanceof LivingEntity, range * range);
        return r == null ? null : (LivingEntity) r.getEntity();
    }
}
