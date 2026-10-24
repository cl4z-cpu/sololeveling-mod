package com.sololeveling.mod.net;

import com.sololeveling.mod.stats.Shop;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class BuyPacket {
    private final int index;

    public BuyPacket(int index) { this.index = index; }

    public static void encode(BuyPacket m, FriendlyByteBuf buf) { buf.writeInt(m.index); }
    public static BuyPacket decode(FriendlyByteBuf buf) { return new BuyPacket(buf.readInt()); }

    public static void handle(BuyPacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p != null) Shop.buy(p, m.index);
        });
        ctx.get().setPacketHandled(true);
    }
}
