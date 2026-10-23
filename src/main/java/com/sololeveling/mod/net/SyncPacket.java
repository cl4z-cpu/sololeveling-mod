package com.sololeveling.mod.net;

import com.sololeveling.mod.client.ClientStats;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SyncPacket {
    private final int[] v;

    public SyncPacket(int[] v) { this.v = v; }

    public static void encode(SyncPacket m, FriendlyByteBuf buf) {
        for (int i = 0; i < 8; i++) buf.writeInt(m.v[i]);
    }

    public static SyncPacket decode(FriendlyByteBuf buf) {
        int[] a = new int[8];
        for (int i = 0; i < 8; i++) a[i] = buf.readInt();
        return new SyncPacket(a);
    }

    public static void handle(SyncPacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientStats.v = m.v);
        ctx.get().setPacketHandled(true);
    }
}
