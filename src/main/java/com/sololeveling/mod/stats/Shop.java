package com.sololeveling.mod.stats;

import com.sololeveling.mod.item.ModItems;
import com.sololeveling.mod.net.ModNetwork;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public class Shop {
    public static class Entry {
        public final String label;
        public final int price;
        public final Supplier<ItemStack> stack;
        Entry(String label, int price, Supplier<ItemStack> stack) {
            this.label = label; this.price = price; this.stack = stack;
        }
    }

    public static final int RESET_INDEX = 8;

    public static final List<Entry> ENTRIES = List.of(
        new Entry("Pan x16", 15, () -> new ItemStack(Items.BREAD, 16)),
        new Entry("Pocion de curacion", 50, () -> PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRONG_HEALING)),
        new Entry("Manzana dorada x2", 120, () -> new ItemStack(Items.GOLDEN_APPLE, 2)),
        new Entry("Perlas de Ender x4", 160, () -> new ItemStack(Items.ENDER_PEARL, 4)),
        new Entry("Diamantes x3", 300, () -> new ItemStack(Items.DIAMOND, 3)),
        new Entry("Lingote netherita", 800, () -> new ItemStack(Items.NETHERITE_INGOT)),
        new Entry("Matacaballeros", 500, () -> new ItemStack(ModItems.KNIGHT_KILLER.get())),
        new Entry("Daga Rey Demonio", 2500, () -> new ItemStack(ModItems.DEMON_DAGGER.get())),
        new Entry("Reiniciar stats", 400, () -> ItemStack.EMPTY)
    );

    public static void buy(ServerPlayer p, int idx) {
        if (idx < 0 || idx >= ENTRIES.size()) return;
        Entry e = ENTRIES.get(idx);
        int gold = PlayerStats.get(p, "gold");
        if (gold < e.price) {
            p.sendSystemMessage(Component.literal("[Sistema] No tienes suficiente oro.").withStyle(ChatFormatting.RED));
            return;
        }
        if (idx == RESET_INDEX) {
            PlayerStats.resetStats(p);
            p.sendSystemMessage(Component.literal("[Sistema] Stats reiniciadas. Tus puntos fueron devueltos."));
        } else {
            ItemStack s = e.stack.get();
            if (!p.getInventory().add(s)) p.drop(s, false);
            p.sendSystemMessage(Component.literal("[Sistema] Compraste: " + e.label));
        }
        PlayerStats.set(p, "gold", gold - e.price);
        ModNetwork.sync(p);
    }
}
