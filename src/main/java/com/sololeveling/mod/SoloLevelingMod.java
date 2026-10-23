package com.sololeveling.mod;

import com.sololeveling.mod.item.ModItems;
import com.sololeveling.mod.net.ModNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SoloLevelingMod.MODID)
public class SoloLevelingMod {
    public static final String MODID = "sololeveling";

    public SoloLevelingMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
        ModNetwork.register();
    }
}
