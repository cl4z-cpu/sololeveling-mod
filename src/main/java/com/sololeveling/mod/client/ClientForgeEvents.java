package com.sololeveling.mod.client;

import com.sololeveling.mod.SoloLevelingMod;
import com.sololeveling.mod.stats.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
        GuiGraphics g = e.getGuiGraphics();
        int[] v = ClientStats.v;
        int need = Math.max(1, PlayerStats.xpNeeded(v[0]));
        boolean quest = v[9] == 1;
        int bottom = quest ? 76 : 50;

        g.fill(4, 4, 126, bottom, 0x90000000);
        g.drawString(mc.font, "Rango " + PlayerStats.rank(v[0]) + "  Nv." + v[0], 8, 8, 0x66CCFF);
        int w = (int) (114.0 * Math.min(v[1], need) / need);
        g.fill(8, 20, 122, 24, 0xFF1A1A2E);
        g.fill(8, 20, 8 + w, 24, 0xFF4FB3FF);
        g.drawString(mc.font, "XP " + v[1] + "/" + need, 8, 27, 0xAAAAAA);
        g.drawString(mc.font, "Oro: " + v[8], 8, 38, 0xFFD700);
        if (quest) {
            g.drawString(mc.font, "Mision: " + v[10] + "/" + v[11], 8, 52, 0xFFFFFF);
            g.drawString(mc.font, "Tiempo: " + (v[12] / 60) + ":" + String.format("%02d", v[12] % 60), 8, 63,
                    v[12] < 60 ? 0xFF5555 : 0xDDDDDD);
        }
    }
}
