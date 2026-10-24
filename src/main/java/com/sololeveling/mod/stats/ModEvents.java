package com.sololeveling.mod.stats;

import com.sololeveling.mod.SoloLevelingMod;
import com.sololeveling.mod.net.ModNetwork;
import com.sololeveling.mod.power.Scheduler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = SoloLevelingMod.MODID)
public class ModEvents {
    private static int counter = 0;

    @SubscribeEvent
    public static void onKill(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead instanceof Player) {
            if (dead instanceof ServerPlayer sp) Quests.discardPenalty(sp);
            return;
        }
        if (e.getSource().getEntity() instanceof ServerPlayer sp) {
            boolean hostile = dead instanceof Enemy;
            double goldBonus = 1.0 + 0.02 * PlayerStats.get(sp, "sense");
            double base = hostile ? dead.getMaxHealth() / 2.0 : dead.getMaxHealth() / 10.0;
            int gold = (int) Math.max(1, Math.round(base * goldBonus));
            PlayerStats.set(sp, "gold", PlayerStats.get(sp, "gold") + gold);

            if (dead.getPersistentData().hasUUID(Quests.TAG_OWNER)) Quests.onPenaltyKilled(sp, dead);
            Quests.onKill(sp, dead);

            double xpBonus = 1.0 + 0.01 * PlayerStats.get(sp, "int");
            int xp = (int) Math.max(1, Math.round(dead.getMaxHealth() / 2.0 * xpBonus));
            PlayerStats.addXp(sp, xp);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Scheduler.tick();
        if (++counter >= 20) {
            counter = 0;
            Quests.tickSecond(ServerLifecycleHooks.getCurrentServer());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent e) { Scheduler.clear(); }

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
