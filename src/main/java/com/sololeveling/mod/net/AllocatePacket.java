package com.sololeveling.mod.net;

import com.sololeveling.mod.stats.PlayerStats;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class AllocatePacket {
    private final int stat;

    public AllocatePacket(int stat) { this.stat = stat; }

    public static void encode(AllocatePacket m, FriendlyByteBuf buf) { buf.writeInt(m.stat); }
    public static AllocatePacket decode(FriendlyByteBuf buf) { return new AllocatePacket(buf.readInt()); }

    public static void handle(AllocatePacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p == null || m.stat < 0 || m.stat >= PlayerStats.KEYS.length) return;
            int points = PlayerStats.get(p, "points");
            if (points <= 0) return;
            String k = PlayerStats.KEYS[m.stat];
            PlayerStats.set(p, "points", points - 1);
            PlayerStats.set(p, k, PlayerStats.get(p, k) + 1);
            PlayerStats.apply(p);
            ModNetwork.sync(p);
        });
        ctx.get().setPacketHandled(true);
    }
}
