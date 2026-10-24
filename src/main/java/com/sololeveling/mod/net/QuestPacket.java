package com.sololeveling.mod.net;

import com.sololeveling.mod.stats.Quests;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class QuestPacket {
    public QuestPacket() {}

    public static void encode(QuestPacket m, FriendlyByteBuf buf) {}
    public static QuestPacket decode(FriendlyByteBuf buf) { return new QuestPacket(); }

    public static void handle(QuestPacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) Quests.accept(p);
        });
        ctx.get().setPacketHandled(true);
    }
}
