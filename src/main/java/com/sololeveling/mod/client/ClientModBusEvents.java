package com.sololeveling.mod.client;

import com.sololeveling.mod.SoloLevelingMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SoloLevelingMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModBusEvents {
    public static final KeyMapping OPEN = new KeyMapping(
            "key.sololeveling.open", GLFW.GLFW_KEY_K, "key.categories.sololeveling");

    @SubscribeEvent
    public static void onKeys(RegisterKeyMappingsEvent e) { e.register(OPEN); }
}
