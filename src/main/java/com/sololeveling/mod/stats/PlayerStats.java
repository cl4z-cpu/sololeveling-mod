package com.sololeveling.mod.stats;

import com.sololeveling.mod.net.ModNetwork;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class PlayerStats {
    public static final String ROOT = "sololeveling";
    public static final String[] KEYS = {"str", "agi", "vit", "sense", "int"};
    public static final int MAX_LEVEL = 100;
    public static final int POINTS_PER_LEVEL = 5;

    private static final UUID[] IDS = {
        UUID.fromString("a1b2c3d4-0001-4000-8000-000000000001"),
        UUID.fromString("a1b2c3d4-0002-4000-8000-000000000002"),
        UUID.fromString("a1b2c3d4-0003-4000-8000-000000000003"),
        UUID.fromString("a1b2c3d4-0004-4000-8000-000000000004"),
        UUID.fromString("a1b2c3d4-0005-4000-8000-000000000005")
    };

    public static CompoundTag tag(Player p) {
        CompoundTag d = p.getPersistentData();
        if (!d.contains(ROOT, 10)) d.put(ROOT, new CompoundTag());
        return d.getCompound(ROOT);
    }

    public static int get(Player p, String k) { return tag(p).getInt(k); }
    public static void set(Player p, String k, int v) { tag(p).putInt(k, v); }
    public static int level(Player p) { return Math.max(1, get(p, "level")); }
    public static int xpNeeded(int level) { return 100 + (level - 1) * 40; }

    public static String rank(int lvl) {
        if (lvl < 10) return "E";
        if (lvl < 20) return "D";
        if (lvl < 30) return "C";
        if (lvl < 45) return "B";
        if (lvl < 60) return "A";
        return "S";
    }

    /** [nivel, xp, puntos, str, agi, vit, sense, int] */
    public static int[] toArray(Player p) {
        int[] a = new int[8];
        a[0] = level(p); a[1] = get(p, "xp"); a[2] = get(p, "points");
        for (int i = 0; i < 5; i++) a[3 + i] = get(p, KEYS[i]);
        return a;
    }

    public static void addXp(ServerPlayer p, int amount) {
        int lvl = level(p);
        if (lvl >= MAX_LEVEL) return;
        int xp = get(p, "xp") + amount;
        boolean up = false;
        while (lvl < MAX_LEVEL && xp >= xpNeeded(lvl)) {
            xp -= xpNeeded(lvl);
            lvl++;
            set(p, "points", get(p, "points") + POINTS_PER_LEVEL);
            up = true;
        }
        set(p, "level", lvl);
        set(p, "xp", xp);
        if (up) {
            p.sendSystemMessage(Component.literal("[Sistema] ¡Has subido de nivel! Nivel " + lvl
                    + " | Rango " + rank(lvl) + " | Puntos disponibles: " + get(p, "points") + " (tecla K)"));
            p.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
        }
        ModNetwork.sync(p);
    }

    public static void apply(Player p) {
        mod(p, Attributes.ATTACK_DAMAGE, 0, 0.5 * get(p, "str"), AttributeModifier.Operation.ADDITION);
        mod(p, Attributes.MOVEMENT_SPEED, 1, Math.min(0.5, 0.01 * get(p, "agi")), AttributeModifier.Operation.MULTIPLY_BASE);
        mod(p, Attributes.MAX_HEALTH, 2, 1.0 * get(p, "vit"), AttributeModifier.Operation.ADDITION);
        mod(p, Attributes.LUCK, 3, 0.1 * get(p, "sense"), AttributeModifier.Operation.ADDITION);
    }

    private static void mod(Player p, Attribute attr, int i, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(IDS[i]);
        if (amount > 0) inst.addPermanentModifier(new AttributeModifier(IDS[i], "sololeveling_" + KEYS[i], amount, op));
    }
}
