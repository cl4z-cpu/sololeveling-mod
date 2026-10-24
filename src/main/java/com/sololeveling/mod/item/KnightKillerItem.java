package com.sololeveling.mod.item;

import com.sololeveling.mod.power.Powers;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class KnightKillerItem extends SwordItem {
    public KnightKillerItem() {
        super(ModTiers.KNIGHT_KILLER, 5, -1.2f, new Item.Properties());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setSecondsOnFire(6);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            Powers.groundSlam(sp);
            player.getCooldowns().addCooldown(this, 100);
            stack.hurtAndBreak(3, player, pl -> pl.broadcastBreakEvent(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("Pasivo: quema al enemigo al golpear").withStyle(ChatFormatting.RED));
        tip.add(Component.literal("Clic derecho: Golpe al suelo (onda hacia adelante)").withStyle(ChatFormatting.GOLD));
        tip.add(Component.literal("Enfriamiento: 5 s").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tip, flag);
    }
}
