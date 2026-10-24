package com.sololeveling.mod.client;

import com.sololeveling.mod.net.AllocatePacket;
import com.sololeveling.mod.net.BuyPacket;
import com.sololeveling.mod.net.ModNetwork;
import com.sololeveling.mod.net.QuestPacket;
import com.sololeveling.mod.stats.PlayerStats;
import com.sololeveling.mod.stats.Shop;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StatsScreen extends Screen {
    private static final String[] LANG = {
        "gui.sololeveling.str", "gui.sololeveling.agi", "gui.sololeveling.vit",
        "gui.sololeveling.sense", "gui.sololeveling.int"
    };
    private static final String[] TABS = {"Estado", "Tienda", "Misión"};

    private int tab = 0;
    private Button questBtn;

    public StatsScreen() { super(Component.translatable("gui.sololeveling.title")); }

    @Override
    protected void init() { rebuild(); }

    private void rebuild() {
        clearWidgets();
        questBtn = null;
        int cx = this.width / 2, top = this.height / 2 - 95;

        for (int i = 0; i < 3; i++) {
            final int t = i;
            Button b = Button.builder(Component.literal(TABS[i]), x -> { tab = t; rebuild(); })
                    .bounds(cx - 99 + i * 67, top + 12, 64, 18).build();
            b.active = (tab != i);
            addRenderableWidget(b);
        }

        if (tab == 0) {
            for (int i = 0; i < 5; i++) {
                final int idx = i;
                addRenderableWidget(Button.builder(Component.literal("+"),
                        b -> ModNetwork.CH.sendToServer(new AllocatePacket(idx)))
                        .bounds(cx + 60, top + 78 + i * 20, 20, 18).build());
            }
        } else if (tab == 1) {
            int colW = Math.min(154, (this.width - 20) / 2 - 2);
            for (int i = 0; i < Shop.ENTRIES.size(); i++) {
                final int idx = i;
                Shop.Entry e = Shop.ENTRIES.get(i);
                int col = i % 2, row = i / 2;
                int x = cx - colW - 2 + col * (colW + 4);
                addRenderableWidget(Button.builder(Component.literal(e.label + "  " + e.price + "G"),
                        b -> ModNetwork.CH.sendToServer(new BuyPacket(idx)))
                        .bounds(x, top + 52 + row * 22, colW, 20).build());
            }
        } else {
            questBtn = Button.builder(Component.literal("Aceptar misión"),
                    b -> ModNetwork.CH.sendToServer(new QuestPacket()))
                    .bounds(cx - 60, top + 150, 120, 20).build();
            addRenderableWidget(questBtn);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = this.width / 2, top = this.height / 2 - 95;
        int[] v = ClientStats.v;
        g.drawCenteredString(this.font, this.title, cx, top, 0x66CCFF);

        if (tab == 0) {
            g.drawString(this.font, Component.literal("Nivel " + v[0] + "  |  Rango " + PlayerStats.rank(v[0])), cx - 80, top + 38, 0xFFFFFF);
            g.drawString(this.font, Component.literal("XP " + v[1] + " / " + PlayerStats.xpNeeded(v[0])), cx - 80, top + 50, 0xAAAAAA);
            g.drawString(this.font, Component.literal("Puntos: " + v[2] + "   Oro: " + v[8]), cx - 80, top + 62, 0xFFD700);
            for (int i = 0; i < 5; i++) {
                g.drawString(this.font, Component.translatable(LANG[i]).append(": " + v[3 + i]), cx - 80, top + 83 + i * 20, 0xFFFFFF);
            }
        } else if (tab == 1) {
            g.drawCenteredString(this.font, Component.literal("Oro: " + v[8]), cx, top + 38, 0xFFD700);
        } else {
            if (questBtn != null) questBtn.active = (v[9] == 0);
            if (v[9] == 1) {
                g.drawString(this.font, Component.literal("Misión activa: elimina " + v[11] + " monstruos"), cx - 110, top + 40, 0xFFFFFF);
                g.drawString(this.font, Component.literal("Progreso: " + v[10] + " / " + v[11]), cx - 110, top + 54, 0x66FF66);
                g.drawString(this.font, Component.literal("Tiempo: " + (v[12] / 60) + ":" + String.format("%02d", v[12] % 60)),
                        cx - 110, top + 68, v[12] < 60 ? 0xFF5555 : 0xFFFFFF);
            } else {
                g.drawString(this.font, Component.literal("No tienes ninguna misión activa."), cx - 110, top + 40, 0xAAAAAA);
            }
            g.drawWordWrap(this.font, Component.literal(
                    "Si se acaba el tiempo sin completarla, aparece el Guardián de la Penalización, un monstruo muy fuerte. "
                    + "Si mueres, el Guardián desaparece. Si lo derrotas, ganas una gran recompensa."),
                    cx - 110, top + 90, 220, 0xCCCCCC);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
