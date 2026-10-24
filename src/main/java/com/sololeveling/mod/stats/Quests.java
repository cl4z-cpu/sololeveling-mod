package com.sololeveling.mod.stats;

import com.sololeveling.mod.net.ModNetwork;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class Quests {
    public static final String TAG_OWNER = "sl_penalty_owner";
    private static final String PENALTY = "penalty";

    public static boolean active(ServerPlayer p) { return PlayerStats.get(p, "q_active") == 1; }

    public static void accept(ServerPlayer p) {
        if (active(p)) {
            p.sendSystemMessage(Component.literal("[Sistema] Ya tienes una misión activa.").withStyle(ChatFormatting.RED));
            return;
        }
        if (penaltyAlive(p)) {
            p.sendSystemMessage(Component.literal("[Sistema] Derrota al Guardián antes de aceptar otra misión.").withStyle(ChatFormatting.RED));
            return;
        }
        int lvl = PlayerStats.level(p);
        int target = Math.min(40, 8 + lvl);
        int time = 180 + target * 10;
        PlayerStats.set(p, "q_active", 1);
        PlayerStats.set(p, "q_prog", 0);
        PlayerStats.set(p, "q_target", target);
        PlayerStats.set(p, "q_time", time);
        p.sendSystemMessage(Component.literal("[Sistema] MISIÓN: elimina " + target + " monstruos en "
                + (time / 60) + ":" + String.format("%02d", time % 60) + ". Si fallas, aparecerá el Guardián de la Penalización.")
                .withStyle(ChatFormatting.YELLOW));
        ModNetwork.sync(p);
    }

    public static void onKill(ServerPlayer p, LivingEntity dead) {
        if (!active(p) || !(dead instanceof Enemy)) return;
        int prog = PlayerStats.get(p, "q_prog") + 1;
        int target = PlayerStats.get(p, "q_target");
        PlayerStats.set(p, "q_prog", prog);
        if (prog >= target) complete(p);
    }

    private static void complete(ServerPlayer p) {
        int lvl = PlayerStats.level(p);
        PlayerStats.set(p, "q_active", 0);
        PlayerStats.set(p, "gold", PlayerStats.get(p, "gold") + 60 + lvl * 12);
        PlayerStats.set(p, "points", PlayerStats.get(p, "points") + 2);
        p.sendSystemMessage(Component.literal("[Sistema] ¡Misión completada! +" + (60 + lvl * 12) + " oro, +2 puntos.")
                .withStyle(ChatFormatting.GREEN));
        p.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
        PlayerStats.addXp(p, 60 + lvl * 20);
    }

    /** Se llama cada segundo. */
    public static void tickSecond(MinecraftServer server) {
        if (server == null) return;
        for (ServerPlayer p : new ArrayList<>(server.getPlayerList().getPlayers())) {
            if (!active(p)) continue;
            int t = PlayerStats.get(p, "q_time") - 1;
            if (t <= 0) {
                PlayerStats.set(p, "q_time", 0);
                PlayerStats.set(p, "q_active", 0);
                p.sendSystemMessage(Component.literal("[Sistema] ¡Misión fallida! Aparece el Guardián de la Penalización.")
                        .withStyle(ChatFormatting.DARK_RED));
                spawnPenalty(p);
            } else {
                PlayerStats.set(p, "q_time", t);
            }
            ModNetwork.sync(p);
        }
    }

    private static void setAttr(LivingEntity e, Attribute a, double v) {
        AttributeInstance inst = e.getAttribute(a);
        if (inst != null) inst.setBaseValue(v);
    }

    private static void spawnPenalty(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        Vindicator m = EntityType.VINDICATOR.create(level);
        if (m == null) return;
        int lvl = PlayerStats.level(p);
        Vec3 pos = p.position();
        boolean placed = false;
        for (int i = 0; i < 12 && !placed; i++) {
            double ang = level.random.nextDouble() * Math.PI * 2;
            double dist = 4 + level.random.nextDouble() * 3;
            m.moveTo(pos.x + Math.cos(ang) * dist, pos.y, pos.z + Math.sin(ang) * dist, 0f, 0f);
            placed = level.noCollision(m);
        }
        if (!placed) m.moveTo(pos.x, pos.y, pos.z, 0f, 0f);

        m.finalizeSpawn(level, level.getCurrentDifficultyAt(m.blockPosition()), MobSpawnType.EVENT, null, null);
        setAttr(m, Attributes.MAX_HEALTH, 120 + lvl * 6);
        m.setHealth(m.getMaxHealth());
        setAttr(m, Attributes.ATTACK_DAMAGE, 6 + lvl * 0.25);
        setAttr(m, Attributes.MOVEMENT_SPEED, 0.33);
        setAttr(m, Attributes.FOLLOW_RANGE, 64);
        setAttr(m, Attributes.ARMOR, 8);
        setAttr(m, Attributes.KNOCKBACK_RESISTANCE, 0.6);
        m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
        m.setDropChance(EquipmentSlot.MAINHAND, 0f);
        m.setCustomName(Component.literal("Guardián de la Penalización").withStyle(ChatFormatting.DARK_RED));
        m.setCustomNameVisible(true);
        m.setPersistenceRequired();
        m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 999999, 0, false, false));
        m.getPersistentData().putUUID(TAG_OWNER, p.getUUID());
        level.addFreshEntity(m);
        m.setTarget(p);
        PlayerStats.tag(p).putUUID(PENALTY, m.getUUID());

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(m.position());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }

    public static boolean penaltyAlive(ServerPlayer p) {
        var tag = PlayerStats.tag(p);
        if (!tag.hasUUID(PENALTY)) return false;
        UUID id = tag.getUUID(PENALTY);
        for (ServerLevel lvl : p.server.getAllLevels()) {
            Entity e = lvl.getEntity(id);
            if (e != null && e.isAlive()) return true;
        }
        tag.remove(PENALTY);
        return false;
    }

    /** El jugador murió: el Guardián desaparece. */
    public static void discardPenalty(ServerPlayer p) {
        var tag = PlayerStats.tag(p);
        if (!tag.hasUUID(PENALTY)) return;
        UUID id = tag.getUUID(PENALTY);
        boolean found = false;
        for (ServerLevel lvl : p.server.getAllLevels()) {
            Entity e = lvl.getEntity(id);
            if (e != null) { e.discard(); found = true; }
        }
        tag.remove(PENALTY);
        if (found) {
            p.sendSystemMessage(Component.literal("[Sistema] El Guardián se desvanece...").withStyle(ChatFormatting.GRAY));
        }
    }

    public static void onPenaltyKilled(ServerPlayer killer, LivingEntity dead) {
        UUID owner = dead.getPersistentData().getUUID(TAG_OWNER);
        ServerPlayer op = killer.server.getPlayerList().getPlayer(owner);
        if (op != null) PlayerStats.tag(op).remove(PENALTY);
        int lvl = PlayerStats.level(killer);
        PlayerStats.set(killer, "gold", PlayerStats.get(killer, "gold") + 200 + lvl * 20);
        killer.sendSystemMessage(Component.literal("[Sistema] ¡Derrotaste al Guardián! +" + (200 + lvl * 20) + " oro.")
                .withStyle(ChatFormatting.GOLD));
        PlayerStats.addXp(killer, 150 + lvl * 30);
    }
}
