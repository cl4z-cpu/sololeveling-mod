package com.sololeveling.mod.item;

import com.sololeveling.mod.power.Powers;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class DemonDaggerItem extends SwordItem {
    public DemonDaggerItem() {
        super(ModTiers.DEMON_KING, 8, -1.0f, new Item.Properties().fireResistant());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (player.isShiftKeyDown()) {
                Powers.thousandCuts(sp);
                player.getCooldowns().addCooldown(this, 300);
                stack.hurtAndBreak(6, player, pl -> pl.broadcastBreakEvent(hand));
            } else {
                Powers.slash(sp);
                player.getCooldowns().addCooldown(this, 50);
                stack.hurtAndBreak(2, player, pl -> pl.broadcastBreakEvent(hand));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("Pasivo: envenena al golpear").withStyle(ChatFormatting.DARK_GREEN));
        tip.add(Component.literal("Clic derecho: Tajo hacia adelante").withStyle(ChatFormatting.LIGHT_PURPLE));
        tip.add(Component.literal("Agachado + clic derecho: Mil cortes").withStyle(ChatFormatting.LIGHT_PURPLE));
        tip.add(Component.literal("Enfriamiento: 2.5 s / 15 s").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tip, flag);
    }
}
