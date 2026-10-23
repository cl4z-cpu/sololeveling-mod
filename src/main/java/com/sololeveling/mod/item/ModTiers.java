package com.sololeveling.mod.item;

import java.util.function.Supplier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum ModTiers implements Tier {
    KNIGHT_KILLER(2, 700, 8.0f, 1.0f, 12, () -> Ingredient.of(Items.IRON_INGOT)),
    DEMON_KING(4, 2500, 9.0f, 3.0f, 18, () -> Ingredient.of(Items.NETHERITE_INGOT));

    private final int level, uses, enchant;
    private final float speed, damage;
    private final Supplier<Ingredient> repair;

    ModTiers(int level, int uses, float speed, float damage, int enchant, Supplier<Ingredient> repair) {
        this.level = level; this.uses = uses; this.speed = speed;
        this.damage = damage; this.enchant = enchant; this.repair = repair;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return damage; }
    @Override public int getLevel() { return level; }
    @Override public int getEnchantmentValue() { return enchant; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
}
