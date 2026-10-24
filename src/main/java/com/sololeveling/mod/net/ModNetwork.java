package com.sololeveling.mod.net;

import com.sololeveling.mod.SoloLevelingMod;
import com.sololeveling.mod.stats.PlayerStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String V = "2";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SoloLevelingMod.MODID, "main"), () -> V, V::equals, V::equals);

    public static void register() {
        int id = 0;
        CH.registerMessage(id++, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle);
        CH.registerMessage(id++, AllocatePacket.class, AllocatePacket::encode, AllocatePacket::decode, AllocatePacket::handle);
        CH.registerMessage(id++, BuyPacket.class, BuyPacket::encode, BuyPacket::decode, BuyPacket::handle);
        CH.registerMessage(id++, QuestPacket.class, QuestPacket::encode, QuestPacket::decode, QuestPacket::handle);
    }

    public static void sync(ServerPlayer p) {
        CH.send(PacketDistributor.PLAYER.with(() -> p), new SyncPacket(PlayerStats.toArray(p)));
    }
}
