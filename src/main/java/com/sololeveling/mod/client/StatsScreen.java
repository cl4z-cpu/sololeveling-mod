package com.sololeveling.mod.client;

import com.sololeveling.mod.net.AllocatePacket;
import com.sololeveling.mod.net.ModNetwork;
import com.sololeveling.mod.stats.PlayerStats;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StatsScreen extends Screen {
    private static final String[] LANG = {
        "gui.sololeveling.str", "gui.sololeveling.agi", "gui.sololeveling.vit",
        "gui.sololeveling.sense", "gui.sololeveling.int"
    };

    public StatsScreen() { super(Component.translatable("gui.sololeveling.title")); }

    @Override
    protected void init() {
        int cx = this.width / 2, top = this.height / 2 - 75;
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal("+"),
                    b -> ModNetwork.CH.sendToServer(new AllocatePacket(idx)))
                    .bounds(cx + 60, top + 56 + i * 22, 20, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = this.width / 2, top = this.height / 2 - 75;
        int[] v = ClientStats.v;
        g.drawCenteredString(this.font, this.title, cx, top, 0x66CCFF);
        g.drawString(this.font, Component.literal("Nivel " + v[0] + "  |  Rango " + PlayerStats.rank(v[0])), cx - 80, top + 16, 0xFFFFFF);
        g.drawString(this.font, Component.literal("XP " + v[1] + " / " + PlayerStats.xpNeeded(v[0])), cx - 80, top + 28, 0xAAAAAA);
        g.drawString(this.font, Component.literal("Puntos: " + v[2]), cx - 80, top + 42, 0xFFD700);
        for (int i = 0; i < 5; i++) {
            g.drawString(this.font, Component.translatable(LANG[i]).append(": " + v[3 + i]), cx - 80, top + 62 + i * 22, 0xFFFFFF);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
