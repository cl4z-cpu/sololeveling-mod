package com.sololeveling.mod.stats;

import com.sololeveling.mod.SoloLevelingMod;
import com.sololeveling.mod.net.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoloLevelingMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onKill(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead instanceof Player) return;
        if (e.getSource().getEntity() instanceof ServerPlayer sp) {
            double bonus = 1.0 + 0.01 * PlayerStats.get(sp, "int");
            int xp = (int) Math.max(1, Math.round(dead.getMaxHealth() / 2.0 * bonus));
            PlayerStats.addXp(sp, xp);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) { PlayerStats.apply(sp); ModNetwork.sync(sp); }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) { PlayerStats.apply(sp); ModNetwork.sync(sp); }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        var old = e.getOriginal().getPersistentData();
        if (old.contains(PlayerStats.ROOT, 10)) {
            e.getEntity().getPersistentData().put(PlayerStats.ROOT, old.getCompound(PlayerStats.ROOT).copy());
        }
    }
}
