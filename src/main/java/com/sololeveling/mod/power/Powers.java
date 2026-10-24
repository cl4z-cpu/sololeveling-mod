package com.sololeveling.mod.power;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Powers {

    private static boolean canTarget(ServerPlayer p, LivingEntity t) {
        if (t == p || !t.isAlive()) return false;
        if (t instanceof Player || t instanceof AbstractVillager || t instanceof ArmorStand) return false;
        if (t instanceof TamableAnimal ta && ta.isOwnedBy(p)) return false;
        return true;
    }

    private static void strike(ServerPlayer p, LivingEntity t, float dmg) {
        t.invulnerableTime = 0;
        t.hurt(p.damageSources().playerAttack(p), dmg);
    }

    private static void poison(LivingEntity t) {
        t.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
    }

    private static double groundY(ServerLevel level, double x, double y, double z) {
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos bp = BlockPos.containing(x, y + dy, z);
            if (!level.getBlockState(bp).getCollisionShape(level, bp).isEmpty()) return bp.getY() + 1.0;
        }
        return y;
    }

    /** Matacaballeros: golpe al suelo con onda de fuego hacia adelante. */
    public static void groundSlam(ServerPlayer p) {
        final ServerLevel level = p.serverLevel();
        final float dmg = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3f;
        Vec3 look = p.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0E-4) flat = new Vec3(0, 0, 1);
        final Vec3 d = flat.normalize();
        final Vec3 side = new Vec3(-d.z, 0, d.x);
        final Vec3 start = p.position();
        final Set<UUID> already = new HashSet<>();

        level.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.7f, 1.3f);
        level.sendParticles(ParticleTypes.EXPLOSION, start.x + d.x, start.y + 0.3, start.z + d.z, 1, 0, 0, 0, 0);

        for (int step = 1; step <= 10; step++) {
            final int s = step;
            Scheduler.later(step, () -> {
                Vec3 c = start.add(d.scale(s * 1.6));
                double gy = groundY(level, c.x, start.y, c.z);
                for (int w = -2; w <= 2; w++) {
                    Vec3 q = c.add(side.scale(w * 0.7));
                    BlockState bs = level.getBlockState(BlockPos.containing(q.x, gy - 0.5, q.z));
                    if (bs.isAir()) bs = Blocks.DIRT.defaultBlockState();
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, bs), q.x, gy + 0.2, q.z, 10, 0.25, 0.3, 0.25, 0.1);
                    level.sendParticles(ParticleTypes.FLAME, q.x, gy + 0.3, q.z, 4, 0.2, 0.2, 0.2, 0.02);
                }
                Vec3 center = new Vec3(c.x, gy + 1.0, c.z);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(center, 4.4, 3.0, 4.4), t -> canTarget(p, t) && already.add(t.getUUID()))) {
                    strike(p, e, dmg);
                    e.setSecondsOnFire(6);
                    e.push(d.x * 0.9, 0.55, d.z * 0.9);
                    e.hurtMarked = true;
                }
            });
        }
    }

    /** Daga del Rey Demonio: tajo hacia adelante. */
    public static void slash(ServerPlayer p) {
        final ServerLevel level = p.serverLevel();
        final float dmg = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.4f;
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0E-4) side = new Vec3(1, 0, 0);
        side = side.normalize();
        Set<UUID> already = new HashSet<>();

        level.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.7f);
        for (int i = 1; i <= 10; i++) {
            Vec3 c = eye.add(look.scale(i));
            if (i % 2 == 0) {
                for (int k = -2; k <= 2; k++) {
                    Vec3 q = c.add(side.scale(k * 0.8));
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, q.x, q.y, q.z, 1, 0, 0, 0, 0);
                }
            }
            level.sendParticles(ParticleTypes.WITCH, c.x, c.y, c.z, 6, 0.4, 0.4, 0.4, 0.05);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                    AABB.ofSize(c, 3.2, 2.4, 3.2), t -> canTarget(p, t) && already.add(t.getUUID()))) {
                strike(p, e, dmg);
                poison(e);
                e.push(look.x * 0.6, 0.25, look.z * 0.6);
                e.hurtMarked = true;
            }
        }
    }

    /** Daga del Rey Demonio: mil cortes alrededor. */
    public static void thousandCuts(ServerPlayer p) {
        final ServerLevel level = p.serverLevel();
        final float base = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE);
        level.playSound(null, p.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8f, 1.5f);
        for (int w = 0; w < 14; w++) {
            final boolean last = (w == 13);
            Scheduler.later(w * 2, () -> {
                if (p.isRemoved() || !p.isAlive()) return;
                Vec3 c = p.position();
                int n = last ? 30 : 14;
                for (int k = 0; k < n; k++) {
                    double ang = level.random.nextDouble() * Math.PI * 2;
                    double r = 0.8 + level.random.nextDouble() * 3.4;
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            c.x + Math.cos(ang) * r, c.y + 0.4 + level.random.nextDouble() * 1.6,
                            c.z + Math.sin(ang) * r, 1, 0, 0, 0, 0);
                }
                level.sendParticles(ParticleTypes.WITCH, c.x, c.y + 1.0, c.z, 25, 2.5, 0.8, 2.5, 0.1);
                level.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                        0.9f, 0.8f + level.random.nextFloat() * 0.6f);
                float dmg = base * (last ? 1.2f : 0.3f);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(c, 9.0, 4.0, 9.0), t -> canTarget(p, t) && p.distanceToSqr(t) < 20.25)) {
                    strike(p, e, dmg);
                    poison(e);
                }
            });
        }
    }
}
