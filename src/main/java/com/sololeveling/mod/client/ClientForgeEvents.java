package com.sololeveling.mod.client;

import com.sololeveling.mod.SoloLevelingMod;
import com.sololeveling.mod.stats.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoloLevelingMod.MODID, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (ClientModBusEvents.OPEN.consumeClick()) {
            if (mc.player != null && mc.screen == null) mc.setScreen(new StatsScreen());
        }
    }

    @SubscribeEvent
    public static void onHud(RenderGuiOverlayEvent.Post e) {
        if (e.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        Minecraft mc = Minecraft.getInstance();
        int[] v = ClientStats.v;
        String s = "Rango " + PlayerStats.rank(v[0]) + "  Nv." + v[0] + "  XP " + v[1] + "/" + PlayerStats.xpNeeded(v[0]);
        e.getGuiGraphics().drawString(mc.font, s, 6, 6, 0x66CCFF);
    }
}
